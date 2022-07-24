package com.fms.sakar.sakir.configuration;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;

@Singleton
public class ExchangeProducer {

    @Inject
    Properties properties;

    @Produces
    public BinanceApiRestClient getBinanceApiRestClient() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(properties.getProperty("binance.api.key"), properties.getProperty("binance.api.secret"));

        return factory.newRestClient();
    }

    @Produces
    public BinanceApiWebSocketClient getBinanceApiWebSocketClient() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(properties.getProperty("binance.api.key"), properties.getProperty("binance.api.secret"));

        return factory.newWebSocketClient();
    }
}
