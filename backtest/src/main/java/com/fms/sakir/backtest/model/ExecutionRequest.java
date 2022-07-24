package com.fms.sakir.backtest.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExecutionRequest implements Serializable {

    private List<String> strategies;

    private List<String> tickers;

    private String startDate;

    private String endDate;

    private String interval;
}
