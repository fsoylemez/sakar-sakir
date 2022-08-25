package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

public class SarCciStrategy extends SimpleStrategy {

    public final Integer cciBarCount;

    public final Integer entryPointThreshold;

    public final Integer exitPointThreshold;

    public SarCciStrategy(String strategyName, Integer cciBarCount, Integer entryPointThreshold, Integer exitPointThreshold) {
        super(strategyName);
        this.cciBarCount = cciBarCount;
        this.entryPointThreshold = entryPointThreshold;
        this.exitPointThreshold = exitPointThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator cci = new CCIIndicator(series, cciBarCount);
        ParabolicSarIndicator sar = new ParabolicSarIndicator(series);
        ClosePriceIndicator close = new ClosePriceIndicator(series);

        Rule entryRule = new CrossedDownIndicatorRule(sar, close)
                .and(new OverIndicatorRule(cci, entryPointThreshold));

        Rule exitRule = new CrossedUpIndicatorRule(sar, close)
                .or(new UnderIndicatorRule(cci, exitPointThreshold));

        return new BaseStrategy(entryRule, exitRule);
    }
}
