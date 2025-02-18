package com.fsoylemez.sakir.backtest.model;

import lombok.Data;

import java.util.List;

@Data
public class ExecutionResult {

    private List<StrategyPerformanceSummary> strategyPerformances;

    private List<ExecutionResponse> executionResponses;
}
