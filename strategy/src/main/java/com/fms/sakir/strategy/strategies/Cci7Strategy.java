package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class Cci7Strategy extends SimpleStrategy {

    private final Integer cciBarCount;

    public Cci7Strategy(String strategyName, Integer cciBarCount) {
        super(strategyName);
        this.cciBarCount = cciBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator cci = new CCIIndicator(series,cciBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(cci, -100);

        Rule exitRule = new CrossedDownIndicatorRule(cci, 100);

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
