package com.fms.sakir.backtest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StrategyPerformanceSummary {

    private String strategyName;

    private double totalReturn;
}
