package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.indicator.ParabolicSarExIndicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

public class StochCciSar2Strategy extends SimpleStrategy {

    public final Integer stochBarCount;

    public final Integer cciBarCount;

    public final Integer entryPointThreshold;

    public StochCciSar2Strategy(String strategyName, Integer stochBarCount, Integer cciBarCount, Integer entryPointThreshold) {
        super(strategyName);
        this.stochBarCount = stochBarCount;
        this.cciBarCount = cciBarCount;
        this.entryPointThreshold = entryPointThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        CCIIndicator cci = new CCIIndicator(series, cciBarCount);
        ParabolicSarExIndicator sar = new ParabolicSarExIndicator(series);
        ClosePriceIndicator close = new ClosePriceIndicator(series);

        Rule entryRule = new UnderIndicatorRule(stochK, entryPointThreshold).and(new CrossedUpIndicatorRule(stochK, stochD))
                .and(new IsRisingRule(cci, 1));

        Rule exitRule = new CrossedUpIndicatorRule(sar, close).or(new CrossedDownIndicatorRule(cci, -100));

        return new BaseStrategy(entryRule, exitRule);
    }
}
