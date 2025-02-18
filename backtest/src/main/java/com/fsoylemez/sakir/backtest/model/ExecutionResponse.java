package com.fsoylemez.sakir.backtest.model;

import com.fsoylemez.sakir.strategy.model.StrategyExecutionResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExecutionResponse implements Serializable {

    private String ticker;

    private String winnerStrategy;

    private List<StrategyExecutionResponse> strategyExecutionResponses;
}
