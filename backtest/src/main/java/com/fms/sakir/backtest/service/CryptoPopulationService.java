package com.fms.sakir.backtest.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.model.PopulateHistory;
import com.fms.sakir.backtest.util.DateUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.fms.sakir.backtest.util.DateUtils.getDueDate;
import static com.fms.sakir.backtest.util.StringUtils.buildDbName;

@ApplicationScoped
public class CryptoPopulationService {

    @Inject
    CouchDbService dbService;

    @Inject
    BinanceApiRestClient binanceApi;

    public void populate(String symbol, String interval, Integer year, Integer month, Integer day) {

        CandlestickInterval candlestickInterval = CandlestickInterval.valueOf(interval);
        String pairId = buildDbName(symbol, candlestickInterval);
        PopulateHistory history = dbService.getHistory(pairId);
        long lastExecuted;
        if (history != null) {
            lastExecuted = history.getLastExecuted();
        } else if(year != null && month != null && day != null) {
            lastExecuted = ZonedDateTime.of(LocalDateTime.of(year, month, day, 0, 0), DateUtils.getZoneId()).toInstant().toEpochMilli();
        } else {
            lastExecuted = ZonedDateTime.of(LocalDateTime.of(2022, 1, 1, 0, 0), DateUtils.getZoneId()).toInstant().toEpochMilli();
        }


        while(lastExecuted < getDueDate(candlestickInterval)) {
            List<Candlestick> candlesticks = binanceApi.getCandlestickBars(symbol.toUpperCase(), candlestickInterval, 1000, lastExecuted, null);

            dbService.write(pairId, candlesticks);
            lastExecuted = candlesticks.stream().mapToLong(Candlestick::getCloseTime).max().getAsLong();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        dbService.writeHistory(new PopulateHistory(pairId, lastExecuted));
    }

}
