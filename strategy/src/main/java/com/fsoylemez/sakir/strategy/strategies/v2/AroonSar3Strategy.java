package com.fsoylemez.sakir.strategy.strategies.v2;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;


public class AroonSar3Strategy extends SimpleStrategy {

    private final Integer aroonCount;

    public AroonSar3Strategy(String strategyName, Integer aroonCount) {
        super(strategyName);
        this.aroonCount = aroonCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonCount);
        ParabolicSarIndicator sar = new ParabolicSarIndicator(series);
        ClosePriceIndicator close = new ClosePriceIndicator(series);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new UnderIndicatorRule(sar, close));

        Rule exitRule = new CrossedUpIndicatorRule(sar, close)
                .or(new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator()).and(new OverIndicatorRule(sar, close)));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
