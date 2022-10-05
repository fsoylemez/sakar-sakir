package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.rules.*;


public class ComboStrategy extends SimpleStrategy {

    private Integer aroonBarCount;

    private Integer cmfBarCount;

    private Integer stochBarCount;

    public ComboStrategy(String strategyName, Integer aroonBarCount, Integer cmfBarCount, Integer stochBarCount) {
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
        FisherIndicator fisher = new FisherIndicator(series);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        Rule entryRule = new AndRule(new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator()), new IsRisingRule(fisher, 1))
                .or(new UnderIndicatorRule(stochK, 30).and(new CrossedUpIndicatorRule(stochK, stochD)).and(new IsRisingRule(cmf, 1)));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator()).and((new IsFallingRule(fisher, 1)))
                .or(new OverIndicatorRule(stochK, 70).and(new CrossedDownIndicatorRule(stochK, stochD)).and(new IsFallingRule(cmf, 1)));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
