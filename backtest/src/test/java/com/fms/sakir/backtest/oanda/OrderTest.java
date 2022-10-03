package com.fms.sakir.backtest.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.*;
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
public class OrderTest {

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
    void marketOpenTest() {

        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            MarketOrderRequest order = new MarketOrderRequest();
            order.setInstrument("AUD_NZD");
            order.setUnits(200000);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = ctx.order.create(request);
            log.info(mapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    void limitOpenTest() {

        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            LimitOrderRequest order = new LimitOrderRequest();
            order.setInstrument("AUD_NZD");
            order.setUnits(-100000);
            order.setPrice(1.13360);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = ctx.order.create(request);
            log.info(mapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    void marketOpenStopLossTest() {

        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            MarketOrderRequest order = new MarketOrderRequest();
            order.setInstrument("AUD_NZD");
            order.setUnits(-1000000);

            StopLossDetails stopLossDetails = new StopLossDetails();
            stopLossDetails.setDistance("2");
            order.setStopLossOnFill(stopLossDetails);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = ctx.order.create(request);
            log.info(mapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    void marketOpenTakeProfitTest() {

        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            MarketOrderRequest order = new MarketOrderRequest();
            order.setInstrument("AUD_NZD");
            order.setUnits(-1000000);

            TakeProfitDetails takeProfitDetails = new TakeProfitDetails();
            takeProfitDetails.setPrice("1.13512");
            order.setTakeProfitOnFill(takeProfitDetails);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = ctx.order.create(request);
            log.info(mapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
