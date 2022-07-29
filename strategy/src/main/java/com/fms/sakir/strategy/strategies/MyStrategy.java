package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class MyStrategy extends SimpleStrategy {

    private Integer rsi;

    private Double lowerThreshold;

    private Double higherThreshold;

    public MyStrategy(String strategyName, Integer rsi, Double lowerThreshold, Double higherThreshold) {
        super(strategyName);
        this.rsi = rsi;
        this.lowerThreshold = lowerThreshold;
        this.higherThreshold = higherThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }


        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, 7);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator());

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonDownIndicator(), aroon.getAroonUpIndicator())
                .or(new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), 100));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
