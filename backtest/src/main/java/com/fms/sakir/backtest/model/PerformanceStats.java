package com.fms.sakir.backtest.model;

import lombok.Data;

import java.util.List;

@Data
public class PerformanceStats {

    private List<StrategyPerformance> bestTotalReturn;

    private List<StrategyPerformance> bestWinningRate;

    private List<StrategyPerformance> bestWinningRatio;

    private List<StrategyPerformance> bestWorstPerformance;

}
