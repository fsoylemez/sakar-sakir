package com.fsoylemez.sakar.sakir.configuration;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;

import javax.enterprise.context.RequestScoped;
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

    @Produces
    @RequestScoped
    public Context getOandaContext() {
       return new ContextBuilder(properties.getProperty("oanda.url"))
                .setToken(properties.getProperty("oanda.token"))
                .setApplication("sakir")
                .build();
    }
}
