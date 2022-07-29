package com.fms.sakir.backtest.model;

import lombok.Data;

import java.util.List;

@Data
public class ExecutionResult {

    private List<StrategyPerformance> strategyPerformances;

    private List<ExecutionResponse> executionResponses;
}
