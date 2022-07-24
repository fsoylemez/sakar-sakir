package com.fms.sakir.backtest.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.mapper.CandleStickMapper;
import java.util.Collections;
import org.ta4j.core.Bar;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DataFetchService {

    @Inject
    BinanceApiRestClient binanceApi;

    @Inject
    CandleStickMapper mapper;

    public List<Bar> fetchData(String symbol, CandlestickInterval interval, Long startTime, Long endTime) {
        List<Candlestick> candlesticks = binanceApi.getCandlestickBars(symbol.toUpperCase(), interval, null, startTime, endTime);
        if (candlesticks.isEmpty()) {
            return Collections.emptyList();
        }

        return candlesticks.stream().map(c-> mapper.toBar(c, interval)).collect(Collectors.toList());
    }
}
