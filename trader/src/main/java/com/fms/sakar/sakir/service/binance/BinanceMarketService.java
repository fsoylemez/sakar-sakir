package com.fms.sakar.sakir.service.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class BinanceMarketService {

    @Inject
    BinanceApiRestClient binanceApiRestClient;

    public List<Candlestick> getCandlestickBars(String symbol, CandlestickInterval interval, Integer size) {
        return binanceApiRestClient.getCandlestickBars(symbol.toUpperCase(), interval, size, null, null);
    }
}
