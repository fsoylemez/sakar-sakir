package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class CciMAStrategy extends SimpleStrategy {

    private final Integer cciBarCount;

    public CciMAStrategy(String strategyName, Integer cciBarCount) {
        super(strategyName);
        this.cciBarCount = cciBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator cci = new CCIIndicator(series, cciBarCount);
        SMAIndicator ema = new SMAIndicator(cci, cciBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(cci, ema);

        Rule exitRule = new CrossedDownIndicatorRule(cci, ema);

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
