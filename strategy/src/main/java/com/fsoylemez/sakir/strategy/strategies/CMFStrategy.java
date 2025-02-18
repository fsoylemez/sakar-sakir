package com.fsoylemez.sakir.strategy.strategies;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class CMFStrategy extends SimpleStrategy {

    private Integer barCount;

    private Double lowerThreshold;

    private Double higherThreshold;

    public CMFStrategy(String strategyName, Integer barCount, Double lowerThreshold, Double higherThreshold) {
        super(strategyName);
        this.barCount = barCount;
        this.lowerThreshold = lowerThreshold;
        this.higherThreshold = higherThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, barCount);

        Rule entryRule = new CrossedUpIndicatorRule(cmf, lowerThreshold);

        Rule exitRule = new CrossedDownIndicatorRule(cmf, higherThreshold);


        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}

