package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class SarStrategy extends SimpleStrategy {


    public SarStrategy(String strategyName) {
        super(strategyName);
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ParabolicSarIndicator sar = new ParabolicSarIndicator(series);
        ClosePriceIndicator close = new ClosePriceIndicator(series);

        Rule entryRule = new CrossedDownIndicatorRule(sar, close);

        Rule exitRule = new CrossedUpIndicatorRule(sar, close);

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
