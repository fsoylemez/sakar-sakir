package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.candles.BearishEngulfingIndicator;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

public class EngulfingCciStrategy extends SimpleStrategy {

    private final Integer cciBarCount;

    public EngulfingCciStrategy(String strategyName, Integer cciBarCount) {
        super(strategyName);
        this.cciBarCount = cciBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        BullishEngulfingIndicator bullish = new BullishEngulfingIndicator(series);
        BearishEngulfingIndicator bearish = new BearishEngulfingIndicator(series);
        CCIIndicator cci = new CCIIndicator(series, cciBarCount);

        Rule entryRule = new BooleanIndicatorRule(bullish);

        Rule exitRule = new BooleanIndicatorRule(bearish).or(new CrossedDownIndicatorRule(cci, -100));

        return new BaseStrategy(entryRule, exitRule);
    }
}
