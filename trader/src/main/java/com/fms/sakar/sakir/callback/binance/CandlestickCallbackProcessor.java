package com.fms.sakar.sakir.callback.binance;

import com.binance.api.client.BinanceApiWebSocketCallback;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.fms.sakar.sakir.service.RunnerService;
import com.fms.sakar.sakir.service.binance.BinancePositionService;
import com.fms.sakar.sakir.util.DateUtils;
import com.fms.sakir.strategy.exception.SakirException;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fms.sakar.sakir.execution.ExecutionConstants.*;

@Slf4j
public class CandlestickCallbackProcessor implements BinanceApiWebSocketCallback<CandlestickEvent> {

    private final BinancePositionService binancePositionService;

    private final RunnerService runnerService;

    private final UUID taskId;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final String symbol;

    private final BarSeries barSeries;

    private Long lastOpenTime;

    private final Duration duration;

    private final String strategyName;

    private final Boolean onlyFinalCandles;

    public CandlestickCallbackProcessor(UUID taskId, RunnerService runnerService, BinancePositionService binancePositionService, Map<String, Object> executionParams) {
        this.taskId = taskId;
        this.runnerService = runnerService;
        this.binancePositionService = binancePositionService;

        this.symbol = (String) executionParams.get(SYMBOL);
        this.duration = (Duration) executionParams.get(DURATION);
        this.strategyName = (String) executionParams.get(STRATEGY_NAME);
        this.barSeries = (BarSeries) executionParams.get(BAR_SERIES);
        this.lastOpenTime = (Long) executionParams.get(LAST_OPEN_TIME);
        this.onlyFinalCandles = (Boolean) executionParams.get(ONLY_FINAL_CANDLES);

        running.set(true);
    }

    @Override
    public void onResponse(CandlestickEvent response) {

        if (running.get()) {
            ZonedDateTime closeTime = Instant.ofEpochMilli(response.getCloseTime()).atZone(DateUtils.getZoneId());

            Bar newBar = new BaseBar(duration, closeTime, response.getOpen(),
                    response.getHigh(), response.getLow(),
                    response.getClose(), response.getVolume());

            if (Boolean.TRUE.equals(onlyFinalCandles) && Boolean.FALSE.equals(response.getBarFinal())) {
                return;
            }

            if (Objects.equals(lastOpenTime, response.getOpenTime())) {
                barSeries.addBar(newBar, true);
            } else {
                barSeries.addBar(newBar);
                lastOpenTime = response.getOpenTime();
                //log.info("Added new bar, open time: {}", Instant.ofEpochMilli(lastOpenTime).atZone(DateUtils.getZoneId()));
            }

            try {
                binancePositionService.evaluateEntryOrExit(barSeries, symbol, strategyName);
            } catch (SakirException e) {
                log.error("Evaluate exception:", e);
            }
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        BinanceApiWebSocketCallback.super.onFailure(cause);
        log.error("Websocket failure.", cause);
        log.info("Reconnecting...");
        runnerService.reconnect(taskId);
    }

    @Override
    public void onClosing(int code, String reason) {
        BinanceApiWebSocketCallback.super.onClosing(code, reason);
        log.error("Websocket closing.Code: {} Reason: {}", code, reason);
        if (1001 == code) {
            log.info("Websocket closed.Reinstantiating runner.");
            runnerService.reconnect(taskId);
        }
    }

    public void stopRunning() {
        running.set(false);
    }
}
