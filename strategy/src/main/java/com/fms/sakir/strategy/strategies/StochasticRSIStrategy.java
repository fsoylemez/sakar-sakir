package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.jboss.logging.Logger;
import org.ta4j.core.*;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;


public class StochasticRSIStrategy extends SimpleStrategy {

    private static final Logger log = Logger.getLogger(StochasticRSIStrategy.class);

    private Integer rsi;

    private Double lowerThreshold;

    private Double higherThreshold;

    public StochasticRSIStrategy(String strategyName, Integer rsi, Double lowerThreshold, Double higherThreshold) {
        super(strategyName);
        this.rsi = rsi;
        this.lowerThreshold = lowerThreshold;
        this.higherThreshold = higherThreshold;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(series, rsi);

        Rule entryRule = new CrossedUpIndicatorRule(stochRsi, lowerThreshold);

        Rule exitRule = new CrossedDownIndicatorRule(stochRsi, higherThreshold);

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }
}
