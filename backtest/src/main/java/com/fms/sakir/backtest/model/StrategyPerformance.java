package com.fms.sakir.backtest.model;

import lombok.Data;

@Data
public class StrategyPerformance {

    private String _id;

    private String symbol;

    private String interval;

    private String strategyName;

    private String startDate;

    private String endDate;

    private double totalReturn;

    private int positionCount;
}
