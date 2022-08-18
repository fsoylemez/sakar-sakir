package com.fms.sakir.strategy.strategies.v2;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.rules.*;


public class AroonFisher2Strategy extends SimpleStrategy {

    private Integer aroonBarCount;

    private Integer stochBarCount;

    public AroonFisher2Strategy(String strategyName, Integer aroonBarCount, Integer stochBarCount) {
        super(strategyName);
        this.aroonBarCount = aroonBarCount;
        this.stochBarCount = stochBarCount;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        AroonOscillatorIndicator aroon = new AroonOscillatorIndicator(series, aroonBarCount);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochBarCount);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        FisherIndicator fisher = new FisherIndicator(series);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new UnderIndicatorRule(stochK, 30).and(new CrossedUpIndicatorRule(stochK, stochD)));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new UnderIndicatorRule(stochK, stochD).and(new IsFallingRule(fisher, 1)));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
