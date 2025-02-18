package com.fsoylemez.sakir.backtest.oanda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.instrument.InstrumentPriceResponse;
import com.oanda.v20.primitives.InstrumentName;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Properties;

@Disabled
@Slf4j
@QuarkusTest
public class InstrumentTest {

    @Inject
    Properties properties;

    private Context ctx;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    void initContext() {
        ctx = new ContextBuilder(properties.getProperty("oanda.url"))
                .setToken(properties.getProperty("oanda.token"))
                .setApplication("sakir")
                .build();
    }

    @Test
    void candlesTest() {

        try {
            InstrumentCandlesResponse response = ctx.instrument.candles(new InstrumentCandlesRequest(new InstrumentName("USD_JPY")).setGranularity(CandlestickGranularity.M5));

            log.info(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
           log.error(e.getMessage());
        }
    }

    @Test
    void priceTest() {

        try {
            InstrumentPriceResponse response = ctx.instrument.price(new InstrumentName("USD_JPY"));

            log.info(objectMapper.writeValueAsString(response));
        } catch (RequestException e) {
            log.error(e.getErrorMessage());
        } catch (ExecuteException | JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
