package com.fsoylemez.sakir.strategy.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Builder
@Setter
@Getter
public class StrategyExecutionResponse implements Serializable {

    private String strategyName;

    private Integer positionCount;

    private Double grossReturn;

    private List<PositionSummary> positions;
}
