package com.fms.sakir.backtest.service;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.enums.TiingoInterval;
import com.fms.sakir.backtest.enums.TiingoMarket;
import com.fms.sakir.backtest.model.PopulateHistory;
import com.fms.sakir.backtest.model.tiingo.OhlcData;
import com.fms.sakir.backtest.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.fms.sakir.backtest.util.StringUtils.buildDbName;

@Slf4j
@ApplicationScoped
public class FxStockPopulationService {

    @Inject
    CouchDbService dbService;

    @Inject
    TiingoDataFetchService dataFetchService;

    public void populate(String symbol, String interval, String startDate, String market) throws IOException, InterruptedException {

        CandlestickInterval candlestickInterval = CandlestickInterval.valueOf(interval);
        String pairId = buildDbName(symbol, candlestickInterval);
        PopulateHistory history = dbService.getHistory(pairId);
        LocalDate startLocalDate;
        if (history != null) {
            startLocalDate = LocalDate.ofInstant(Instant.ofEpochMilli(history.getLastExecuted()), DateUtils.getUtcZoneId());
            if (startLocalDate.equals(LocalDate.now())) {
                log.info("Last date is today, skipping {}.", symbol);
                return;
            }
        } else if(startDate != null) {
            startLocalDate = LocalDate.parse(startDate);
        } else {
            startLocalDate = LocalDate.of(2022, 1, 1);
        }

        List<OhlcData> data = dataFetchService.fetchData(symbol, startLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                TiingoInterval.getByCandlestickInterval(candlestickInterval), TiingoMarket.valueOf(market));

        dbService.writeTiingo(pairId, data);
        if (data != null && !data.isEmpty()) {
            long lastExecutedMs = data.stream().mapToLong(OhlcData::getDate).max().getAsLong();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            dbService.writeHistory(new PopulateHistory(pairId, lastExecutedMs));
        } else {
            log.info("No Data found, could not calculate last execution.");
        }
    }
}
