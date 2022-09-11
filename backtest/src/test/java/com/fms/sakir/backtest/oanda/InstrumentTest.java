package com.fms.sakir.backtest.oanda;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.instrument.InstrumentPriceResponse;
import com.oanda.v20.primitives.InstrumentName;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
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

    @Test
    void candlesTest() {
        Context ctx = new ContextBuilder("https://api-fxpractice.oanda.com")
                .setToken(properties.getProperty("oanda.token"))
                .build();

        try {
            InstrumentCandlesResponse response = ctx.instrument.candles(new InstrumentCandlesRequest(new InstrumentName("usdjpy")));

            log.info(response.toString());
        } catch (Exception e) {
           log.error(e.getMessage());
        }
    }

    @Test
    void priceTest() {
        Context ctx = new ContextBuilder("https://api-fxpractice.oanda.com")
                .setToken(properties.getProperty("oanda.token"))
                .build();

        try {
            InstrumentPriceResponse response = ctx.instrument.price(new InstrumentName("usdjpy"));

            log.info(response.toString());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
