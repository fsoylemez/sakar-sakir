package com.fsoylemez.sakir.strategy.model;

import lombok.Data;
import org.ta4j.core.Trade;

import java.io.Serializable;

@Data
public class PositionSummary implements Serializable {

    private Trade.TradeType tradeType;

    private String entryTime;

    private Double entryPrice;

    private Double exitPrice;

    private String exitTime;

    private Double grossReturn;

    private Boolean winning;
}
