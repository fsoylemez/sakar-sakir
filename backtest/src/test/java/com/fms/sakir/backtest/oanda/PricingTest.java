package com.fms.sakir.backtest.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.LimitOrderRequest;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
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
public class PricingTest {

    @Inject
    Properties properties;

    private ObjectMapper mapper = new ObjectMapper();

    private Context ctx;

    @BeforeEach
    void initContext() {
        ctx = new ContextBuilder(properties.getProperty("oanda.url"))
                .setToken(properties.getProperty("oanda.token"))
                .setApplication("sakir")
                .build();
    }
    @Test
    void streamTest() {

        try {
            ctx.pricing.candles(new InstrumentName("AUD_NZD"));
        } catch (RequestException e) {
           log.error("request:", e);
        } catch (ExecuteException e) {
            log.error("execute:", e);
        }
    }

}
