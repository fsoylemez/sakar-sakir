package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.rules.*;


public class ComboSolEthCci2Strategy extends SimpleStrategy {

    private final Integer aroonBarCount;

    private final Integer stochBarCount;

    private final Integer cciBarCount;

    private final Integer cciEntryThreshold;

    private final Integer cciExitThreshold;

    public ComboSolEthCci2Strategy(String strategyName, Integer aroonBarCount, Integer stochBarCount, Integer cciBarCount, Integer cciEntryThreshold, Integer cciExitThreshold) {
        super(strategyName);
        this.aroonBarCount = aroonBarCount;
        this.stochBarCount = stochBarCount;
        this.cciBarCount = cciBarCount;
        this.cciEntryThreshold = cciEntryThreshold;
        this.cciExitThreshold = cciExitThreshold;
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
        CCIIndicator cci = new CCIIndicator(series, cciBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new UnderIndicatorRule(stochK, 30).and(new CrossedUpIndicatorRule(stochK, stochD)).and(new IsRisingRule(fisher, 1)))
                .or(new CrossedUpIndicatorRule(cci, cciEntryThreshold));

        Rule exitRule = new CrossedDownIndicatorRule(aroon.getAroonUpIndicator(), aroon.getAroonDownIndicator())
                .or(new OverIndicatorRule(stochK, 70).and(new CrossedDownIndicatorRule(stochK, stochD)).and(new IsFallingRule(fisher, 1)))
                .or(new CrossedDownIndicatorRule(cci, cciExitThreshold));

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
