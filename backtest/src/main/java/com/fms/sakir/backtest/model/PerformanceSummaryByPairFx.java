package com.fms.sakir.backtest.model;

import lombok.Data;

@Data
public class PerformanceSummaryByPairFx {

    private String _id;

    private String _rev;

    private String strategyName;

    private String symbol;

    private Double bestReturn;

    private String bestPeriod;

    private String bestInterval;

    private Double worstReturn;

    private String worstPeriod;

    private String worstInterval;

    private Double averageReturn;

    private Double averagePositionCount;

    private Double m15BestReturn;

    private String m15BestPeriod;

    private String m15BestInterval;

    private Double m15WorstReturn;

    private String m15WorstPeriod;

    private String m15WorstInterval;

    private Double m15AverageReturn;

    private Double m15AveragePositionCount;

    private Double m5BestReturn;

    private String m5BestPeriod;

    private String m5BestInterval;

    private Double m5WorstReturn;

    private String m5WorstPeriod;

    private String m5WorstInterval;

    private Double m5AverageReturn;

    private Double m5AveragePositionCount;

}
