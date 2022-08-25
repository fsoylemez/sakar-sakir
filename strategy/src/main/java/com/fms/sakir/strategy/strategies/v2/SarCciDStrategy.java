package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class SarCciDStrategy extends SimpleStrategy {

    public final Integer cciBarCount;

    public final Integer exitPointThreshold;

    public final Double fisherThreshold;

    public SarCciDStrategy(String strategyName, Integer cciBarCount, Integer exitPointThreshold, Double fisherThreshold) {
        super(strategyName);
        this.cciBarCount = cciBarCount;
        this.exitPointThreshold = exitPointThreshold;
        this.fisherThreshold = fisherThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator cci = new CCIIndicator(series, cciBarCount);
        ParabolicSarIndicator sar = new ParabolicSarIndicator(series);
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        FisherIndicator fisher = new FisherIndicator(series);

        Rule entryRule = new CrossedDownIndicatorRule(sar, close)
                .and(new OverIndicatorRule(fisher, fisherThreshold).or(new OverIndicatorRule(cci, 100)));

        Rule exitRule = new CrossedUpIndicatorRule(sar, close)
                .or(new UnderIndicatorRule(cci, exitPointThreshold));

        return new BaseStrategy(entryRule, exitRule);
    }
}
