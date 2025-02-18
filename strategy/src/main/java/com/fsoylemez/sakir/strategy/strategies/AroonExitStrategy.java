package com.fsoylemez.sakir.strategy.strategies;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class AroonExitStrategy extends SimpleStrategy {

    private final Integer aroonBarCount;

    public AroonExitStrategy(String strategyName, Integer aroonBarCount) {
        super(strategyName);
        this.aroonBarCount = aroonBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }


        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonBarCount);

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
