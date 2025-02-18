package com.fsoylemez.sakir.strategy.strategies.v2;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.candles.BearishEngulfingIndicator;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;

public class EngulfingStrategy extends SimpleStrategy {

    public EngulfingStrategy(String strategyName) {
        super(strategyName);
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        BullishEngulfingIndicator bullish = new BullishEngulfingIndicator(series);
        BearishEngulfingIndicator bearish = new BearishEngulfingIndicator(series);

        Rule entryRule = new BooleanIndicatorRule(bullish);

        Rule exitRule = new BooleanIndicatorRule(bearish);

        return new BaseStrategy(entryRule, exitRule);
    }
}
