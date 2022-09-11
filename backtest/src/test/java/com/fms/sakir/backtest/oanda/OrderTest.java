package com.fms.sakir.backtest.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.*;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Properties;

@Disabled
@Slf4j
@QuarkusTest
public class OrderTest {

    @Inject
    Properties properties;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void marketOpenTest() {
        Context ctx = new ContextBuilder("https://api-fxpractice.oanda.com")
                .setToken(properties.getProperty("oanda.token"))
                .build();

        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            MarketOrderRequest order = new MarketOrderRequest();
            order.setInstrument("AUD_NZD");
            order.setUnits(-1000);
            order.setTimeInForce(TimeInForce.GTC);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = ctx.order.create(request);
            log.info(mapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    void limitOpenTest() {
        Context ctx = new ContextBuilder("https://api-fxpractice.oanda.com")
                .setToken(properties.getProperty("oanda.token"))
                .build();

        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            LimitOrderRequest order = new LimitOrderRequest();
            order.setInstrument("AUD_NZD");
            order.setUnits(-1000);
            order.setPrice(1.12122);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = ctx.order.create(request);
            log.info(mapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
