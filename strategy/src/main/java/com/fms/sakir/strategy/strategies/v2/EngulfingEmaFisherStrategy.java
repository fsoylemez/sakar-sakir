package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.candles.BearishEngulfingIndicator;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.IsFallingRule;
import org.ta4j.core.rules.OverIndicatorRule;

public class EngulfingEmaFisherStrategy extends SimpleStrategy {

    private Integer emaBarCount;

    public EngulfingEmaFisherStrategy(String strategyName, Integer emaBarCount) {
        super(strategyName);
        this.emaBarCount = emaBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        EMAIndicator ema = new EMAIndicator(close, emaBarCount);
        BullishEngulfingIndicator bullish = new BullishEngulfingIndicator(series);
        BearishEngulfingIndicator bearish = new BearishEngulfingIndicator(series);
        FisherIndicator fisherIndicator = new FisherIndicator(series);

        Rule entryRule = new OverIndicatorRule(close, ema).and(new BooleanIndicatorRule(bullish));

        Rule exitRule = new BooleanIndicatorRule(bearish).or(new IsFallingRule(fisherIndicator, 1));

        return new BaseStrategy(entryRule, exitRule);
    }
}
