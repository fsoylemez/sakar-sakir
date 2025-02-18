package com.fsoylemez.sakir.strategy.strategies.v2;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.IsFallingRule;
import org.ta4j.core.rules.OverIndicatorRule;


public class CciFisherStrategy extends SimpleStrategy {

    private final Integer cciBarCount;

    private final Integer cciEntryThreshold;

    private final Integer cciExitThreshold;

    public CciFisherStrategy(String strategyName, Integer cciBarCount, Integer cciEntryThreshold, Integer cciExitThreshold) {
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
        FisherIndicator fisher = new FisherIndicator(series);

        Rule entryRule = new CrossedUpIndicatorRule(cci, cciEntryThreshold)
                .and(new OverIndicatorRule(fisher, -2));

        Rule exitRule = new IsFallingRule(fisher, 2).or(new CrossedDownIndicatorRule(cci, cciExitThreshold));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
