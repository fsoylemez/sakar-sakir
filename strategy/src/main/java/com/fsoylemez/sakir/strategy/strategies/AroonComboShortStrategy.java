package com.fsoylemez.sakir.strategy.strategies;

import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.rules.*;


public class AroonComboShortStrategy extends SimpleStrategy {

    private Integer aroonBarCount;

    private Integer cmfBarCount;

    private Integer stochBarCount;

    public AroonComboShortStrategy(String strategyName, Integer aroonBarCount, Integer cmfBarCount, Integer stochBarCount) {
        super(strategyName);
        this.aroonBarCount = aroonBarCount;
        this.cmfBarCount = cmfBarCount;
        this.stochBarCount = stochBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonBarCount);
        ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfBarCount);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        Rule exitRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new IsRisingRule(cmf, 1));

        Rule entryRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .and(new OrRule(new UnderIndicatorRule(cmf, 0),
                new CrossedDownIndicatorRule(stochK, stochD)));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.SELL;
    }
}
