package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.IsFallingRule;
import org.ta4j.core.rules.UnderIndicatorRule;


public class AroonStochV2Strategy extends SimpleStrategy {

    private final Integer aroonCount;

    private final Integer stochBarCount;

    public AroonStochV2Strategy(String strategyName, Integer aroonCount, Integer stochBarCount) {
        super(strategyName);
        this.aroonCount = aroonCount;
        this.stochBarCount = stochBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonCount);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new UnderIndicatorRule(stochK, 70));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new IsFallingRule(stochK, 1));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
