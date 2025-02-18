package com.fsoylemez.sakar.sakir.service.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.NewOrderResponseType;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.CancelOrderResponse;
import com.fsoylemez.sakar.sakir.model.trade.SpotOrderCancelRequest;
import com.fsoylemez.sakar.sakir.model.trade.SpotOrderOpenRequest;
import com.fsoylemez.sakar.sakir.util.NumberUtils;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.num.DecimalNum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class BinanceSpotTradeService {

    @Inject
    BinanceApiRestClient binanceApiRestClient;

    @Inject
    BinanceAccountService accountService;

    public NewOrderResponse openOrder(SpotOrderOpenRequest request) {
        NewOrder order = new NewOrder(request.getSymbol(), request.getOrderSide(),
                request.getOrderType(), request.getTimeInForce(), request.getQuantity(), request.getPrice());

        return binanceApiRestClient.newOrder(order.newOrderRespType(NewOrderResponseType.ACK));
    }

    public NewOrderResponse marketBuyOrderWithAllUsdt(String symbol, String price) {
        String usdtBalance = accountService.getFreeBalance("USDT");
        if (usdtBalance != null) {
            double availableUsdt = Double.parseDouble(usdtBalance);
            if (availableUsdt > 0) {
                log.info("#####opening position#####");
                DecimalNum quantity1 = DecimalNum.valueOf(availableUsdt / Double.parseDouble(price));
                log.info("Quantity before conversion: {}", quantity1);
                String converted = NumberUtils.convertPrecision(quantity1.toString(), 1);
                log.info("Quantity after conversion: {}", converted);
                return binanceApiRestClient.newOrder(NewOrder.marketBuy(symbol, converted).newOrderRespType(NewOrderResponseType.RESULT));
            }
        }
        return null;
    }

    //TODO decide precision by checking minimum trade amount for the symbol
    public NewOrderResponse marketBuyOrder(String symbol, String quantity) {
        return binanceApiRestClient.newOrder(NewOrder.marketBuy(symbol, NumberUtils.convertPrecision(quantity, 1)).newOrderRespType(NewOrderResponseType.RESULT));
    }

    //TODO decide precision by checking minimum trade amount for the symbol
    public NewOrderResponse marketSellOrder(String symbol, String quantity) {
        return binanceApiRestClient.newOrder(NewOrder.marketSell(symbol, NumberUtils.convertPrecision(quantity, 1)).newOrderRespType(NewOrderResponseType.RESULT));
    }

    public CancelOrderResponse cancelOrder(SpotOrderCancelRequest request) {
        return binanceApiRestClient.cancelOrder(new CancelOrderRequest(request.getSymbol(), request.getOrderId()));
    }


}
