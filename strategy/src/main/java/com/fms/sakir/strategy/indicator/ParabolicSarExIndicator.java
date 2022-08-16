package com.fms.sakir.strategy.indicator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;

import static org.ta4j.core.num.NaN.NaN;

public class ParabolicSarExIndicator extends AbstractIndicator<Num> {

    private final Num maxAcceleration;
    private final Num accelerationIncrement;
    private final Num accelerationStart;
    private Num accelerationFactor;
    private boolean currentTrend; // true if uptrend, false otherwise
    private int startTrendIndex = 0; // index of start bar of the current trend
    private LowPriceIndicator lowPriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private Num currentExtremePoint; // the extreme point of the current calculation
    private Num minMaxExtremePoint; // depending on trend the maximum or minimum extreme point value of trend

    /**
     * Constructor with default parameters
     *
     * @param series the bar series for this indicator
     */
    public ParabolicSarExIndicator(BarSeries series) {
        this(series, series.numOf(0.02), series.numOf(0.2), series.numOf(0.02));

    }

    /**
     * Constructor with custom parameters and default increment value
     *
     * @param series the bar series for this indicator
     * @param aF     acceleration factor
     * @param maxA   maximum acceleration
     */
    public ParabolicSarExIndicator(BarSeries series, Num aF, Num maxA) {
        this(series, aF, maxA, series.numOf(0.02));
    }

    /**
     * Constructor with custom parameters
     *
     * @param series    the bar series for this indicator
     * @param aF        acceleration factor
     * @param maxA      maximum acceleration
     * @param increment the increment step
     */
    public ParabolicSarExIndicator(BarSeries series, Num aF, Num maxA, Num increment) {
        super(series);
        highPriceIndicator = new HighPriceIndicator(series);
        lowPriceIndicator = new LowPriceIndicator(series);
        maxAcceleration = maxA;
        accelerationFactor = aF;
        accelerationIncrement = increment;
        accelerationStart = aF;
    }

    @Override
    public Num getValue(int index) {
        Num sar = NaN;
        if (index == getBarSeries().getBeginIndex()) {
            return sar; // no trend detection possible for the first value
        } else if (index == getBarSeries().getBeginIndex() + 1) {// start trend detection
            currentTrend = getBarSeries().getBar(getBarSeries().getBeginIndex()).getClosePrice()
                    .isLessThan(getBarSeries().getBar(index).getClosePrice());
            if (!currentTrend) { // down trend
                sar = highPriceIndicator.getValue(index); // put sar on high price of candlestick
                currentExtremePoint = sar;
                minMaxExtremePoint = currentExtremePoint;
            } else { // up trend
                sar = lowPriceIndicator.getValue(index); // put sar on low price of candlestick
                currentExtremePoint = sar;
                minMaxExtremePoint = currentExtremePoint;

            }
            return sar;
        }

        Num priorSar = getValue(index - 1);
        if (currentTrend) { // if up trend
            sar = priorSar.plus(accelerationFactor.multipliedBy((currentExtremePoint.minus(priorSar))));
            currentTrend = lowPriceIndicator.getValue(index).isGreaterThan(sar);
            if (!currentTrend) { // check if sar touches the low price
                sar = minMaxExtremePoint; // sar starts at the highest extreme point of previous up trend
                currentTrend = false; // switch to down trend and reset values
                startTrendIndex = index;
                accelerationFactor = accelerationStart;
                currentExtremePoint = getBarSeries().getBar(index).getLowPrice(); // put point on max
                minMaxExtremePoint = currentExtremePoint;
            } else { // up trend is going on
                currentExtremePoint = new HighestValueIndicator(highPriceIndicator, index - startTrendIndex)
                        .getValue(index);
                if (currentExtremePoint.isGreaterThan(minMaxExtremePoint)) {
                    incrementAcceleration();
                    minMaxExtremePoint = currentExtremePoint;
                }

            }
        } else { // downtrend
            sar = priorSar.minus(accelerationFactor.multipliedBy(((priorSar.minus(currentExtremePoint)))));
            currentTrend = highPriceIndicator.getValue(index).isGreaterThanOrEqual(sar);
            if (currentTrend) { // check if switch to up trend
                sar = minMaxExtremePoint; // sar starts at the lowest extreme point of previous down trend
                accelerationFactor = accelerationStart;
                startTrendIndex = index;
                currentExtremePoint = getBarSeries().getBar(index).getHighPrice();
                minMaxExtremePoint = currentExtremePoint;
            } else { // down trend io going on
                currentExtremePoint = new LowestValueIndicator(lowPriceIndicator, index - startTrendIndex)
                        .getValue(index);
                if (currentExtremePoint.isLessThan(minMaxExtremePoint)) {
                    incrementAcceleration();
                    minMaxExtremePoint = currentExtremePoint;
                }
            }
        }
        return sar;
    }

    /**
     * Increments the acceleration factor.
     */
    private void incrementAcceleration() {
        if (accelerationFactor.isGreaterThanOrEqual(maxAcceleration)) {
            accelerationFactor = maxAcceleration;
        } else {
            accelerationFactor = accelerationFactor.plus(accelerationIncrement);
        }
    }
}