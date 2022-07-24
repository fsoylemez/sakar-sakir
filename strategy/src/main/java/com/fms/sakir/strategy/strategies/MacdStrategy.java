package com.fms.sakir.strategy.strategies;

import com.fms.sakir.strategy.base.SimpleStrategy;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;


public class MacdStrategy extends SimpleStrategy {

    private Integer shortBarSize;

    private Integer longBarSize;

    private Integer emaSize;

    public MacdStrategy(String strategyName, Integer shortBarSize, Integer longBarSize, Integer emaSize) {
        this.strategyName = strategyName;
        this.shortBarSize = shortBarSize;
        this.longBarSize = longBarSize;
        this.emaSize = emaSize;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        final MACDIndicator macd = new MACDIndicator(closePrice, shortBarSize, longBarSize);
        final EMAIndicator emaMacd = new EMAIndicator(macd, emaSize);

        Rule entryRule = new OverIndicatorRule(macd, emaMacd);

        Rule exitRule = new UnderIndicatorRule(macd, emaMacd);

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Trade.TradeType getTradeType() {
        return Trade.TradeType.BUY;
    }

}
