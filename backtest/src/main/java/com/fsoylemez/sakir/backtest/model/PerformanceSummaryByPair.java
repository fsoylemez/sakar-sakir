package com.fsoylemez.sakir.backtest.model;

import lombok.Data;

@Data
public class PerformanceSummaryByPair {

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

    private Double h4BestReturn;

    private String h4BestPeriod;

    private String h4BestInterval;

    private Double h4WorstReturn;

    private String h4WorstPeriod;

    private String h4WorstInterval;

    private Double h4AverageReturn;

    private Double h4AveragePositionCount;

    private Double h2BestReturn;

    private String h2BestPeriod;

    private String h2BestInterval;

    private Double h2WorstReturn;

    private String h2WorstPeriod;

    private String h2WorstInterval;

    private Double h2AverageReturn;

    private Double h2AveragePositionCount;

    private Double h1BestReturn;

    private String h1BestPeriod;

    private String h1BestInterval;

    private Double h1WorstReturn;

    private String h1WorstPeriod;

    private String h1WorstInterval;

    private Double h1AverageReturn;

    private Double h1AveragePositionCount;

    private Double m30BestReturn;

    private String m30BestPeriod;

    private String m30BestInterval;

    private Double m30WorstReturn;

    private String m30WorstPeriod;

    private String m30WorstInterval;

    private Double m30AverageReturn;

    private Double m30AveragePositionCount;

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
