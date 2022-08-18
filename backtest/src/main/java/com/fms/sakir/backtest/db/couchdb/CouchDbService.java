package com.fms.sakir.backtest.db.couchdb;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.*;
import com.cloudant.client.api.views.Key;
import com.cloudant.client.api.views.ViewRequest;
import com.cloudant.client.api.views.ViewRequestBuilder;
import com.cloudant.client.api.views.ViewResponse;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.fms.sakir.backtest.model.PopulateHistory;
import com.fms.sakir.backtest.model.StrategyPerformance;
import com.fms.sakir.backtest.util.DateUtils;
import com.fms.sakir.strategy.exception.SakirException;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudant.client.api.query.EmptyExpression.empty;
import static com.cloudant.client.api.query.Expression.*;
import static com.fms.sakir.backtest.util.StringUtils.buildDbName;
import static com.fms.sakir.backtest.util.StringUtils.buildStrategyPerfId;

@ApplicationScoped
public class CouchDbService {

    @Inject
    CloudantClient dbClient;


    public void write(String databaseName, List<Candlestick> priceData) {
        Database database = dbClient.database(databaseName, true);
        database.bulk(priceData);
    }

    public List<Candlestick> read(String symbol, CandlestickInterval interval, long startTime, long endTime) {
        Database database = dbClient.database(buildDbName(symbol, interval), true);
        Selector start = gt("closeTime", startTime);
        Selector end = lt("closeTime", endTime);

        Operation operation = Operation.and(start, end);

        List<Candlestick> data = new ArrayList<>();
        createAscIndex(database, "closeTime");

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(operation).sort(Sort.asc("closeTime"));
        QueryResult<Candlestick> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), Candlestick.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public LocalDateTime getMaxDate(String databaseName) {
        Database database = dbClient.database(databaseName, true);
        Selector selector = empty();
        createDescIndex(database, "date");
        QueryResult<Candlestick> result = database.query(new QueryBuilder(selector).sort(Sort.desc("date")).limit(1).build(), Candlestick.class);

        Optional<Candlestick> dataWithMaxDate = result.getDocs().stream().findFirst();

        return dataWithMaxDate.map(o -> Instant.ofEpochMilli(o.getCloseTime()).atZone(DateUtils.getZoneId()).toLocalDateTime()).orElse(null);
    }

    public boolean databaseExists(String databaseName) {
        return dbClient.getAllDbs().stream().anyMatch(databaseName::equals);
    }

    public void createDescIndex(Database database, String fieldName) {
        String indexName = String.format("by%sDesc", fieldName);
        Indexes indexes = database.listIndexes();
        if (indexes.jsonIndexes().stream().noneMatch(i-> i.getName().equals(indexName))) {
            String indexDefinition = JsonIndex.builder().name(indexName).desc(fieldName).definition();
            database.createIndex(indexDefinition);
        }
    }

    public void createAscIndex(Database database, String fieldName) {
        String indexName = String.format("by%sAsc", fieldName);
        Indexes indexes = database.listIndexes();
        if (indexes.jsonIndexes().stream().noneMatch(i-> i.getName().equals(indexName))) {
            String indexDefinition = JsonIndex.builder().name(indexName).asc(fieldName).definition();
            database.createIndex(indexDefinition);
        }
    }

    public void createView(Database database, String designDocName, String viewName) {
        Map<String, Object> view1 = new HashMap<>();
        view1.put("map", "function(doc){emit(doc.date, doc)}");
        //view1.put("reduce", "function(key, value, rereduce){return sum(values)}");

        Map<String, Object> views = new HashMap<>();
        views.put(viewName, view1);

        Map<String, Object> view_ddoc = new HashMap<>();
        view_ddoc.put("_id", "_design/" + designDocName);
        view_ddoc.put("views", views);

        database.save(view_ddoc);
    }

    public List<Candlestick> useView(Database database, String designDocName, String viewName, Long startKey) throws IOException {
        try {
            database.getDesignDocumentManager().get(designDocName);
        } catch (NoDocumentException e) {
            createView(database, designDocName, viewName);
        }

        //get a ViewRequestBuilder from the database for the chosen view
        ViewRequestBuilder viewBuilder = database.getViewRequestBuilder(designDocName, viewName);

        //build a new request and specify any parameters required
        ViewRequest<Number, Candlestick> request = viewBuilder.newRequest(Key.Type.NUMBER, Candlestick.class)
                .startKey(startKey)
               // .includeDocs(true)
                .build();

        //perform the request and get the response
        ViewResponse<Number, Candlestick> response = request.getResponse();

        return response.getRows().stream().map(ViewResponse.Row::getValue).collect(Collectors.toList());
    }

    public List<Candlestick> readViaView(String databaseName, Long key) throws SakirException {
        try {
            Database database = dbClient.database(databaseName, true);

            return useView(database, "searchOperations", "byDateView", key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new SakirException("Could not search view");
    }

    public void dropDatabases() {
        List<String> databases = dbClient.getAllDbs();
        databases.stream().filter(db -> !db.startsWith("_")).forEach(dbClient::deleteDB);
    }

    public PopulateHistory getHistory(String symbolKey) {
        Database database = dbClient.database("population_history", true);
        if(database.contains(symbolKey)) {
            return database.find(PopulateHistory.class, symbolKey);
        }

        return null;
    }

    public void writeHistory(PopulateHistory history) {
        Database database = dbClient.database("population_history", true);
        PopulateHistory existing = getHistory(history.get_id());
        if(existing != null) {
            existing.setLastExecuted(history.getLastExecuted());
            database.update(existing);
        } else {
            database.save(history);
        }
    }

    public StrategyPerformance getPerformance(String strategyName, String ticker, CandlestickInterval interval, String startDate, String endDate ) {
        Database database = dbClient.database("strategy_performance", true);
        String perfId = buildStrategyPerfId(strategyName, ticker, interval.getIntervalId(), startDate, endDate);
        if(database.contains(perfId)) {
            return database.find(StrategyPerformance.class, perfId);
        }

        return null;
    }

    public void writePerformance(List<StrategyPerformance> strategyPerformances) {
        Database database = dbClient.database("strategy_performance", true);
        database.bulk(strategyPerformances);
    }
}
