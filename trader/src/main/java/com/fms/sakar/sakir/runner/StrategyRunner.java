package com.fms.sakar.sakir.runner;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakar.sakir.callback.binance.v2.CandlestickCallbackProcessorV2;
import com.fms.sakar.sakir.model.runner.RunnerRequest;
import com.fms.sakar.sakir.service.RunnerService;
import com.fms.sakar.sakir.service.binance.BinanceMarketService;
import com.fms.sakar.sakir.service.binance.v2.BinancePositionServiceV2;
import com.fms.sakar.sakir.util.DateUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static com.fms.sakar.sakir.execution.ExecutionConstants.*;

@Slf4j
public class StrategyRunner implements Runnable {

    @Getter
    private final RunnerRequest runnerRequest;

    private final BinanceApiWebSocketClient webSocketClient;

    private final CandlestickCallbackProcessorV2 callbackProcessor;

    private Closeable candlestickStream;


    public StrategyRunner(RunnerRequest runnerRequest, UUID taskId, RunnerService runnerService, BinanceMarketService marketService, BinancePositionServiceV2 binancePositionService, BinanceApiWebSocketClient webSocketClient) {
        log.info("Initializing strategy runner");
        this.runnerRequest = runnerRequest;
        this.webSocketClient = webSocketClient;

        Map<String, Object> executionParams = initExecutionParams(marketService);

        this.callbackProcessor = new CandlestickCallbackProcessorV2(taskId, runnerService, binancePositionService, executionParams);
    }

    @Override
    public void run() {
        log.info("Starting new strategy runner.");
        candlestickStream = webSocketClient.onCandlestickEvent(runnerRequest.getSymbol().toLowerCase(), runnerRequest.getInterval(), callbackProcessor);
    }

    public void stop() throws IOException {
        log.info("Stop signal received, closing web socket.");
        callbackProcessor.stopRunning();
        candlestickStream.close();
    }

    private Map<String, Object> initExecutionParams(BinanceMarketService marketService) {
        String symbol = runnerRequest.getSymbol();

        CandlestickInterval interval = runnerRequest.getInterval();
        Duration duration = DateUtils.intervalToDuration(interval);

        ArrayDeque<Bar> bars = new ArrayDeque<>(BAR_SERIES_SIZE);

        List<Candlestick> candlestickBars = marketService.getCandlestickBars(symbol.toUpperCase(), interval, BAR_SERIES_SIZE);

        candlestickBars.forEach(c -> {
            ZonedDateTime closeTime = Instant.ofEpochMilli(c.getCloseTime()).atZone(DateUtils.getZoneId());

            Bar bar = new BaseBar(duration, closeTime, c.getOpen(),
                    c.getHigh(), c.getLow(),
                    c.getClose(), c.getVolume());

            bars.add(bar);
        });

        Long lastOpenTime = candlestickBars.get(candlestickBars.size() - 1).getOpenTime();

        Map<String, Object> executionParams = new HashMap<>();

        executionParams.put(SYMBOL, symbol);
        executionParams.put(DURATION, duration);
        executionParams.put(STRATEGY_NAME, runnerRequest.getStrategyName());
        executionParams.put(BAR_QUEUE, bars);
        executionParams.put(LAST_OPEN_TIME, lastOpenTime);
        executionParams.put(ONLY_FINAL_CANDLES, runnerRequest.getOnlyFinalCandles());

        return executionParams;
    }
}
