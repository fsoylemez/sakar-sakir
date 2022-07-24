package com.fms.sakir.backtest.model;

import com.fms.sakir.strategy.model.StrategyExecutionResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExecutionResponse implements Serializable {

    private String ticker;

    private List<StrategyExecutionResponse> strategyExecutionResponses;
}
