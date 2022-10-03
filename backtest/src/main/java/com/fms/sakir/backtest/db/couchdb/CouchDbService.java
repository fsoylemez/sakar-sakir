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
import com.fms.sakir.backtest.model.*;
import com.fms.sakir.backtest.model.tiingo.OhlcData;
import com.fms.sakir.backtest.util.BackTestConstants;
import com.fms.sakir.backtest.util.DateUtils;
import com.fms.sakir.strategy.exception.SakirException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ApplicationScoped
public class CouchDbService {

    @Inject
    CloudantClient dbClient;


    public void write(String databaseName, List<Candlestick> priceData) {
        Database database = dbClient.database(databaseName, true);
        database.bulk(priceData);
    }

    public void writeTiingo(String databaseName, List<OhlcData> priceData) {
        Database database = dbClient.database(databaseName, true);
        database.bulk(priceData);
    }

    public List<Candle> read(String symbol, CandlestickInterval interval, long startTime, long endTime) {
        Database database = dbClient.database(buildDbName(symbol, interval), true);
        Selector start = gt("closeTime", startTime);
        Selector end = lt("closeTime", endTime);

        Operation operation = Operation.and(start, end);

        List<Candle> data = new ArrayList<>();
        createAscIndex(database, "closeTime");

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(operation).sort(Sort.asc("closeTime"));
        QueryResult<Candle> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), Candle.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }


    public List<OhlcData> readOhlc(String symbol, CandlestickInterval interval, long startTime, long endTime) {
        Database database = dbClient.database(buildDbName(symbol, interval), true);
        Selector start = gt("date", startTime);
        Selector end = lt("date", endTime);

        Operation operation = Operation.and(start, end);

        List<OhlcData> data = new ArrayList<>();
        createAscIndex(database, "date");

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(operation).sort(Sort.asc("date"));
        QueryResult<OhlcData> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), OhlcData.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public LocalDateTime getMaxDate(String databaseName) {
        Database database = dbClient.database(databaseName, true);
        Selector selector = empty();
        createDescIndex(database, "date");
        QueryResult<Candle> result = database.query(new QueryBuilder(selector).sort(Sort.desc("date")).limit(1).build(), Candle.class);

        Optional<Candle> dataWithMaxDate = result.getDocs().stream().findFirst();

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

    public List<Candle> useView(Database database, String designDocName, String viewName, Long startKey) throws IOException {
        try {
            database.getDesignDocumentManager().get(designDocName);
        } catch (NoDocumentException e) {
            createView(database, designDocName, viewName);
        }

        //get a ViewRequestBuilder from the database for the chosen view
        ViewRequestBuilder viewBuilder = database.getViewRequestBuilder(designDocName, viewName);

        //build a new request and specify any parameters required
        ViewRequest<Number, Candle> request = viewBuilder.newRequest(Key.Type.NUMBER, Candle.class)
                .startKey(startKey)
               // .includeDocs(true)
                .build();

        //perform the request and get the response
        ViewResponse<Number, Candle> response = request.getResponse();

        return response.getRows().stream().map(ViewResponse.Row::getValue).collect(Collectors.toList());
    }

    public List<Candle> readViaView(String databaseName, Long key) throws SakirException {
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

    public void writePerformances(List<StrategyPerformance> strategyPerformances) {
        Database database = dbClient.database("strategy_performance", true);
        database.bulk(strategyPerformances);
    }

    public void updatePerformance(StrategyPerformance performance) {
        Database database = dbClient.database("strategy_performance", false);
        database.update(performance);
    }

    public List<StrategyPerformance> getPerformanceStatistics(String startDate, String endDate) {
        Database database = dbClient.database("strategy_performance", true);
        List<StrategyPerformance> data = new ArrayList<>();
        createAscIndex(database, "closeTime");


        Selector start = eq("startDate", startDate);
        Selector end = eq("endDate", endDate);
        Operation operation = Operation.and(start, end);

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(operation);
        QueryResult<StrategyPerformance> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), StrategyPerformance.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public List<StrategyPerformance> getPerformanceByStrategy(String strategyName) {
        Database database = dbClient.database("strategy_performance", false);
        List<StrategyPerformance> data = new ArrayList<>();

        Selector strategy = eq("strategyName", strategyName);

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(strategy);
        QueryResult<StrategyPerformance> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), StrategyPerformance.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public void writePerformanceSummaries(List<PerformanceSummary> summaries) {
        Database database = dbClient.database("aaa_perf_summary", true);
        database.bulk(summaries);
    }

    public PerformanceSummary getPerformanceSummary(String strategyName) {
        Database database = dbClient.database("aaa_perf_summary", true);
        if(database.contains(strategyName)) {
            return database.find(PerformanceSummary.class, strategyName);
        }

        return null;
    }

    public void updateSummary(PerformanceSummary summary) {
        Database database = dbClient.database("aaa_perf_summary", false);
        database.update(summary);
    }

    public void saveSummary(PerformanceSummary summary) {
        Database database = dbClient.database("aaa_perf_summary", false);
        database.save(summary);
    }

    public void delete(String ticker, CandlestickInterval interval, OhlcData c) {
        Database database = dbClient.database(buildDbName(ticker, interval), false);
        database.remove(c);
    }

    public void delete(String ticker, CandlestickInterval interval, Candle c) {
        Database database = dbClient.database(buildDbName(ticker, interval), false);
        database.remove(c);
    }

    public List<StrategyPerformance> getPerformanceByStrategyFx(String strategyName) {
        Database database = dbClient.database("aaa_perf_summary_fx", false);
        List<StrategyPerformance> data = new ArrayList<>();

        Selector strategy = eq("strategyName", strategyName);
        Selector symbol = in("symbol", BackTestConstants.FX_PAIRS);
        Operation and = Operation.and(strategy, symbol);

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(and);
        QueryResult<StrategyPerformance> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), StrategyPerformance.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public List<StrategyPerformance> getPerformanceByStrategyAndPair(String databaseName, String strategyName, String symbol) {
        Database database = dbClient.database(databaseName, false);
        List<StrategyPerformance> data = new ArrayList<>();

        Selector strategy = eq("strategyName", strategyName);
        Selector symbolSelector = in("symbol", symbol);
        Operation and = Operation.and(strategy, symbolSelector);

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(and);
        QueryResult<StrategyPerformance> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), StrategyPerformance.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public PerformanceSummaryFx getPerformanceSummaryFx(String strategyName) {
        Database database = dbClient.database("aaa_perf_summary_fx", true);
        if(database.contains(strategyName)) {
            return database.find(PerformanceSummaryFx.class, strategyName);
        }

        return null;
    }

    public void updateSummaryFx(PerformanceSummaryFx summary) {
        Database database = dbClient.database("aaa_perf_summary_fx", false);
        database.update(summary);
    }

    public void saveSummaryFx(PerformanceSummaryFx summary) {
        Database database = dbClient.database("aaa_perf_summary_fx", false);

        try {
            database.save(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }

    public List<StrategyPerformance> getForDelete(String databaseName, String[] symbols) {
        Database database = dbClient.database(databaseName, false);
        List<StrategyPerformance> data = new ArrayList<>();

        Selector symbol = in("symbol", symbols);

        String bookmark = null;
        QueryBuilder builder = new QueryBuilder(symbol);
        QueryResult<StrategyPerformance> queryResult;
        do {
            if (StringUtils.isNotEmpty(bookmark)) {
                builder = builder.bookmark(bookmark);
            }
            queryResult = database.query(builder.build(), StrategyPerformance.class);
            data.addAll(queryResult.getDocs());
            bookmark = queryResult.getBookmark();
        } while (!queryResult.getDocs().isEmpty() && StringUtils.isNotEmpty(bookmark));

        return data;
    }

    public void bulk(String databaseName, List<StrategyPerformance> performanceData) {
        Database database = dbClient.database(databaseName, false);
        database.bulk(performanceData);
    }

    public PerformanceSummaryByPairFx getPerformanceSummaryByPairFx(String strategyName, String pair) {
        Database database = dbClient.database("aaa_perf_summary_by_pair_fx", true);
        String id = String.join("_", strategyName, pair);
        if(database.contains(id)) {
            return database.find(PerformanceSummaryByPairFx.class, id);
        }

        return null;
    }

    public void updateSummaryByPairFx(PerformanceSummaryByPairFx summary) {
        Database database = dbClient.database("aaa_perf_summary_by_pair_fx", false);
        try {
            database.update(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }

    public void saveSummaryByPairFx(PerformanceSummaryByPairFx summary) {
        Database database = dbClient.database("aaa_perf_summary_by_pair_fx", false);

        try {
            database.save(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }

    public PerformanceSummaryByPair getPerformanceSummaryByPair(String strategyName, String pair) {
        Database database = dbClient.database("aaa_perf_summary_by_pair", true);
        String id = String.join("_", strategyName, pair);
        if(database.contains(id)) {
            return database.find(PerformanceSummaryByPair.class, id);
        }

        return null;
    }

    public void updateSummaryByPair(PerformanceSummaryByPair summary) {
        Database database = dbClient.database("aaa_perf_summary_by_pair", false);
        try {
            database.update(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }

    public void saveSummaryByPair(PerformanceSummaryByPair summary) {
        Database database = dbClient.database("aaa_perf_summary_by_pair", false);

        try {
            database.save(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }

    public PerformanceSummaryByPairFx getPerformanceSummaryByPairStock(String strategyName, String pair) {
        Database database = dbClient.database("aaa_perf_summary_by_pair_stock", true);
        String id = String.join("_", strategyName, pair);
        if(database.contains(id)) {
            return database.find(PerformanceSummaryByPairFx.class, id);
        }

        return null;
    }

    public void updateSummaryByPairStock(PerformanceSummaryByPairFx summary) {
        Database database = dbClient.database("aaa_perf_summary_by_pair_stock", false);
        try {
            database.update(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }

    public void saveSummaryByPairStock(PerformanceSummaryByPairFx summary) {
        Database database = dbClient.database("aaa_perf_summary_by_pair_stock", false);

        try {
            database.save(summary);
        } catch (Exception e) {
            log.error(summary.toString());
        }
    }
}
