package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.rules.*;


public class ComboSolEthStrategy extends SimpleStrategy {

    private final Integer aroonBarCount;

    private final Integer stochBarCount;

    public ComboSolEthStrategy(String strategyName, Integer aroonBarCount, Integer stochBarCount) {
        this.strategyName = strategyName;
        this.aroonBarCount = aroonBarCount;
        this.stochBarCount = stochBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonBarCount);
        FisherIndicator fisher = new FisherIndicator(series);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new UnderIndicatorRule(stochK, 30).and(new CrossedUpIndicatorRule(stochK, stochD)).and(new IsRisingRule(fisher, 1)));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new OverIndicatorRule(stochK, 70).and(new CrossedDownIndicatorRule(stochK, stochD)).and(new IsFallingRule(fisher, 1)));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
