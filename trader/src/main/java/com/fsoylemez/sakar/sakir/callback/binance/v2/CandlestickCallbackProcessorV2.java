package com.fsoylemez.sakar.sakir.callback.binance.v2;

import com.binance.api.client.BinanceApiWebSocketCallback;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.fsoylemez.sakar.sakir.service.RunnerService;
import com.fsoylemez.sakar.sakir.service.binance.v2.BinancePositionServiceV2;
import com.fsoylemez.sakar.sakir.util.DateUtils;
import com.fsoylemez.sakir.strategy.exception.SakirException;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fsoylemez.sakar.sakir.execution.ExecutionConstants.*;

@Slf4j
public class CandlestickCallbackProcessorV2 implements BinanceApiWebSocketCallback<CandlestickEvent> {

    private final BinancePositionServiceV2 binancePositionService;

    private final RunnerService runnerService;

    private final UUID taskId;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final String symbol;

    private final ArrayDeque<Bar> bars;

    private Long lastOpenTime;

    private final Duration duration;

    private final String strategyName;

    private final Boolean onlyFinalCandles;

    public CandlestickCallbackProcessorV2(UUID taskId, RunnerService runnerService, BinancePositionServiceV2 binancePositionService, Map<String, Object> executionParams) {
        this.taskId = taskId;
        this.runnerService = runnerService;
        this.binancePositionService = binancePositionService;

        this.symbol = (String) executionParams.get(SYMBOL);
        this.duration = (Duration) executionParams.get(DURATION);
        this.strategyName = (String) executionParams.get(STRATEGY_NAME);
        this.bars = (ArrayDeque<Bar>) executionParams.get(BAR_QUEUE);
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
                bars.removeLast();
            } else {
                if (bars.size() >= BAR_SERIES_SIZE) {
                    bars.removeFirst();
                }
                lastOpenTime = response.getOpenTime();
                //log.info("Added new bar, open time: {}", Instant.ofEpochMilli(lastOpenTime).atZone(DateUtils.getZoneId()));
            }

            bars.add(newBar);
            //log.info("Bar queue size: {}", bars.size());
            try {
                binancePositionService.evaluateEntryOrExit(bars, symbol, strategyName);
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
