package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;

public class StochSar2Strategy extends SimpleStrategy {

    public final Integer stochBarCount;

    public final Integer entryPointThreshold;

    public final Integer exitPointThreshold;

    public final Double fisherThreshold;

    public StochSar2Strategy(String strategyName, Integer stochBarCount, Integer entryPointThreshold, Integer exitPointThreshold, Double fisherThreshold) {
        super(strategyName);
        this.stochBarCount = stochBarCount;
        this.entryPointThreshold = entryPointThreshold;
        this.exitPointThreshold = exitPointThreshold;
        this.fisherThreshold = fisherThreshold;
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
        FisherIndicator fisher = new FisherIndicator(series);

        Rule entryRule = new OverIndicatorRule(fisher, fisherThreshold)
                .and(new CrossedDownIndicatorRule(sar, close));

        Rule exitRule = new OverIndicatorRule(stochK, exitPointThreshold).and(new CrossedDownIndicatorRule(stochK, stochD))
                .or(new CrossedUpIndicatorRule(sar, close));

        return new BaseStrategy(entryRule, exitRule);
    }
}
