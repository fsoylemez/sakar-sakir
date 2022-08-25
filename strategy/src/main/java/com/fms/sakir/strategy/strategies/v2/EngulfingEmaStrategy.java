package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.candles.BearishEngulfingIndicator;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;

public class EngulfingEmaStrategy extends SimpleStrategy {

    public EngulfingEmaStrategy(String strategyName) {
        super(strategyName);
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        EMAIndicator ema = new EMAIndicator(close, 200);
        BullishEngulfingIndicator bullish = new BullishEngulfingIndicator(series);
        BearishEngulfingIndicator bearish = new BearishEngulfingIndicator(series);

        Rule entryRule = new OverIndicatorRule(close, ema).and(new BooleanIndicatorRule(bullish));

        Rule exitRule = new BooleanIndicatorRule(bearish);

        return new BaseStrategy(entryRule, exitRule);
    }
}
