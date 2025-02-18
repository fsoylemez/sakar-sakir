package com.fsoylemez.sakir.strategy.strategies;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class AroonStochStrategy extends SimpleStrategy {

    private Integer aroonBarCount;

    private Integer stochBarCount;

    public AroonStochStrategy(String strategyName, Integer aroonBarCount, Integer stochBarCount) {
        super(strategyName);
        this.aroonBarCount = aroonBarCount;
        this.stochBarCount = stochBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonBarCount);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator());
                //.and(new UnderIndicatorRule(stochK, 55));
                //.or(new CrossedUpIndicatorRule(stochK, 20).and(new IsRisingRule(aroon.getAroonUpIndicator(), 1)));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                //.or(new IsFallingRule(aroon.getAroonUpIndicator(), 2).and(new IsRisingRule(aroon.getAroonDownIndicator(), 2)));
                //.and(new UnderIndicatorRule(stochK, stochD).or(new UnderIndicatorRule(stochK, 50)));
                //.or(new IsFallingRule(stochK, 1));
                .or(new CrossedDownIndicatorRule(stochK, 60));
                //.or(new CrossedDownIndicatorRule(stochK, 80));
        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
