package com.fms.sakir.backtest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StrategyPerformance {

    private String strategyName;

    private double totalReturn;
}
