package com.fsoylemez.sakir.backtest.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fsoylemez.sakir.backtest.db.couchdb.CouchDbService;
import com.fsoylemez.sakir.backtest.mapper.CandleStickMapper;
import com.fsoylemez.sakir.backtest.model.Candle;
import org.ta4j.core.Bar;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class BinanceDataFetchService {

    @Inject
    BinanceApiRestClient binanceApi;

    @Inject
    CandleStickMapper mapper;

    @Inject
    CouchDbService dbService;

    public List<Bar> fetchData(String symbol, CandlestickInterval interval, Long startTime, Long endTime) {
        List<Candlestick> candlesticks = binanceApi.getCandlestickBars(symbol.toUpperCase(), interval, null, startTime, endTime);
        if (candlesticks.isEmpty()) {
            return Collections.emptyList();
        }

        return candlesticks.stream().map(c-> mapper.toBar(c, interval)).collect(Collectors.toList());
    }

    public List<Bar> fetchDbData(String symbol, CandlestickInterval interval, Long startTime, Long endTime) {
        List<Candle> candlesticks = dbService.read(symbol, interval, startTime, endTime);
        if (candlesticks.isEmpty()) {
            return Collections.emptyList();
        }

        return candlesticks.stream().map(c-> mapper.toBar(c, interval)).collect(Collectors.toList());
    }
}
