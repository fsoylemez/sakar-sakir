package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.IsFallingRule;
import org.ta4j.core.rules.IsRisingRule;


public class AroonCciStrategy extends SimpleStrategy {

    private final Integer aroonCount;

    private final Integer cciBarCount;

    private final Integer cciCrossDownExit;

    public AroonCciStrategy(String strategyName, Integer aroonCount, Integer cciBarCount, Integer cciCrossDownExit) {
        super(strategyName);
        this.aroonCount = aroonCount;
        this.cciBarCount = cciBarCount;
        this.cciCrossDownExit = cciCrossDownExit;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonCount);
        CCIIndicator cci = new CCIIndicator(series,cciBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new IsRisingRule(cci, 1));

        Rule exitRule = new CrossedDownIndicatorRule(cci, cciCrossDownExit).or(new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new IsFallingRule(cci, 1)));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
