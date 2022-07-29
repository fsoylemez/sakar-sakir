package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class CciStrategy extends SimpleStrategy {

    private final Integer cciBarCount;

    private final Integer cciEntryThreshold;

    private final Integer cciExitThreshold;

    public CciStrategy(String strategyName, Integer cciBarCount, Integer cciEntryThreshold, Integer cciExitThreshold) {
        super(strategyName);
        this.cciBarCount = cciBarCount;
        this.cciEntryThreshold = cciEntryThreshold;
        this.cciExitThreshold = cciExitThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator cci = new CCIIndicator(series,cciBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(cci, cciEntryThreshold);

        Rule exitRule = new CrossedDownIndicatorRule(cci, cciExitThreshold);

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
