package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class StochSarStrategy extends SimpleStrategy {

    public final Integer stochBarCount;

    public final Integer entryPointThreshold;

    public final Integer exitPointThreshold;

    public StochSarStrategy(String strategyName, Integer stochBarCount, Integer entryPointThreshold, Integer exitPointThreshold) {
        super(strategyName);
        this.stochBarCount = stochBarCount;
        this.entryPointThreshold = entryPointThreshold;
        this.exitPointThreshold = exitPointThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        ParabolicSarIndicator sar = new ParabolicSarIndicator(series);
        ClosePriceIndicator close = new ClosePriceIndicator(series);

        Rule entryRule = new UnderIndicatorRule(stochK, entryPointThreshold).and(new CrossedUpIndicatorRule(stochK, stochD))
                .and(new UnderIndicatorRule(sar, close));

        Rule exitRule = new OverIndicatorRule(stochK, exitPointThreshold).and(new CrossedDownIndicatorRule(stochK, stochD))
                .and(new OverIndicatorRule(sar, close));

        return new BaseStrategy(entryRule, exitRule);
    }
}
