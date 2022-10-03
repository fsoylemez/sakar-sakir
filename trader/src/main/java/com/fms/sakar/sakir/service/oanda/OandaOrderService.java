package com.fms.sakar.sakir.service.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.LimitOrderRequest;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.transaction.StopLossDetails;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;

@Slf4j
@ApplicationScoped
public class OandaOrderService {

    @Inject
    Properties properties;

    @Inject
    Context context;

    @Inject
    ObjectMapper objectMapper;

    /*
    * instrument like "AUD_NZD"
    * negative units for short
    * */
    public void market(String instrument, double units) {
        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            MarketOrderRequest order = new MarketOrderRequest();
            order.setInstrument(instrument);
            order.setUnits(units);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = context.order.create(request);
            log.info(objectMapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void limit(String instrument, double units, double price) {
        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            LimitOrderRequest order = new LimitOrderRequest();
            order.setInstrument(instrument);
            order.setUnits(units);
            order.setPrice(price);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = context.order.create(request);
            log.info(objectMapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void marketStopLoss(String instrument, double units, int stopLossPips) {
        try {
            OrderCreateRequest request = new OrderCreateRequest(new AccountID(properties.getProperty("oanda.accountid")));
            MarketOrderRequest order = new MarketOrderRequest();
            order.setInstrument(instrument);
            order.setUnits(units);

            StopLossDetails stopLossDetails = new StopLossDetails();
            stopLossDetails.setDistance(stopLossPips);
            order.setStopLossOnFill(stopLossDetails);

            request.setOrder(order);
            OrderCreateResponse orderCreateResponse = context.order.create(request);
            log.info(objectMapper.writeValueAsString(orderCreateResponse));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
