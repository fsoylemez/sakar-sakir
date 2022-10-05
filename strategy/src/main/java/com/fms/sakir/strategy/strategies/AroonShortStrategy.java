package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.IsFallingRule;
import org.ta4j.core.rules.IsRisingRule;


public class AroonShortStrategy extends SimpleStrategy {

    private Integer barCount;

    public AroonShortStrategy(String strategyName, Integer barCount) {
        super(strategyName);
        this.barCount = barCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, barCount);

        Rule entryRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new IsFallingRule(aroon.getAroonUpIndicator(), 2).and(new IsRisingRule(aroon.getAroonDownIndicator(), 2)));

        Rule exitRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator());
        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
