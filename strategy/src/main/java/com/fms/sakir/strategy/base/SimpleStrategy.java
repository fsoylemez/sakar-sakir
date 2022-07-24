package com.fms.sakir.strategy.base;

import com.fms.sakir.strategy.model.PositionSummary;
import com.fms.sakir.strategy.model.StrategyExecutionResponse;
import lombok.Getter;
import org.jboss.logging.Logger;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.num.Num;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public abstract class SimpleStrategy {

    private static final Logger log = Logger.getLogger(SimpleStrategy.class);

    @Getter
    public String strategyName;

    public abstract Strategy buildStrategy(BarSeries series);

    public abstract Trade.TradeType getTradeType();

    public StrategyExecutionResponse runStrategy(BarSeries series, Boolean showPositions) {

        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(buildStrategy(series), getTradeType());
        Num returnRatio = new GrossReturnCriterion().calculate(series, tradingRecord);

        log.info(strategyName + " , " + tradingRecord.getPositionCount() + " positions " + returnRatio + " return");
        if (tradingRecord.getCurrentPosition().getEntry() != null) {
            log.info("Open Position: " + tradingRecord.getCurrentPosition().toString() + " Entry Time:" + series.getBar(tradingRecord.getCurrentPosition().getEntry().getIndex()).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        StrategyExecutionResponse strategyExecutionResponse = StrategyExecutionResponse.builder().strategyName(strategyName)
                .grossReturn(returnRatio.doubleValue())
                .positionCount(tradingRecord.getPositionCount())
                .build();

        if (Boolean.TRUE.equals(showPositions)) {
            strategyExecutionResponse.setPositions(tradingRecord.getPositions().stream().map(p -> mapPosition(series, p)).collect(Collectors.toList()));
        }

        return strategyExecutionResponse;
    }

    private PositionSummary mapPosition(BarSeries series, Position position) {
        PositionSummary summary = new PositionSummary();
        summary.setTradeType(position.getEntry().getType());
        Trade entry = position.getEntry();
        Bar entryBar = series.getBar(entry.getIndex());
        summary.setEntryTime(entryBar.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        summary.setEntryPrice(entry.getPricePerAsset().doubleValue());
        Trade exit = position.getExit();
        Bar exitBar = series.getBar(exit.getIndex());
        summary.setExitTime(exitBar.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        summary.setExitPrice(exit.getPricePerAsset().doubleValue());
        summary.setGrossReturn(position.getGrossReturn().doubleValue());
        summary.setWinning(position.hasProfit());

        return summary;
    }
}
