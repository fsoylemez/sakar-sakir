package com.fms.sakar.sakir.model.trade;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SpotOrderOpenRequest implements Serializable {
    
    @NotNull private String symbol;

    @NotNull private String price;

    @NotNull private String quantity;

    private OrderSide orderSide = OrderSide.BUY;

    private OrderType orderType = OrderType.LIMIT;

    private TimeInForce timeInForce = TimeInForce.GTC;

    public SpotOrderOpenRequest(String symbol, String price, String quantity) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
    }
}
