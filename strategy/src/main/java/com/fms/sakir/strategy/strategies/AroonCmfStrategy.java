package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.rules.*;


public class AroonCmfStrategy extends SimpleStrategy {

    private Integer aroonBarCount;

    private Integer cmfBarCount;

    public AroonCmfStrategy(String strategyName, Integer aroonBarCount, Integer cmfBarCount) {
        this.strategyName = strategyName;
        this.aroonBarCount = aroonBarCount;
        this.cmfBarCount = cmfBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonBarCount);
        ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new OrRule(new OverIndicatorRule(cmf, 0), new IsRisingRule(cmf, 1)));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new UnderIndicatorRule(cmf, 0));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
