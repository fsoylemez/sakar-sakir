package com.fsoylemez.sakir.strategy.strategies.v2;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.rules.*;

public class StochFisherV2Strategy extends SimpleStrategy {

    public final Integer stochBarCount;

    public final Integer entryPointThreshold;

    public final Integer exitPointThreshold;

    public StochFisherV2Strategy(String strategyName, Integer stochBarCount, Integer entryPointThreshold, Integer exitPointThreshold) {
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
        FisherIndicator fisher = new FisherIndicator(series);

        Rule entryRule = new UnderIndicatorRule(stochK, entryPointThreshold).and(new CrossedUpIndicatorRule(stochK, stochD))
                .and(new IsRisingRule(fisher, 1));

        Rule exitRule = new IsFallingRule(fisher, 2).or(new OverIndicatorRule(stochK, exitPointThreshold).and(new CrossedDownIndicatorRule(stochK, stochD)));

        return new BaseStrategy(entryRule, exitRule);
    }
}
