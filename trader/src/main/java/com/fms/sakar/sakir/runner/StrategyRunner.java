package com.fms.sakar.sakir.runner;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakar.sakir.callback.CandlestickCallbackProcessor;
import com.fms.sakar.sakir.model.runner.RunnerRequest;
import com.fms.sakar.sakir.service.PositionService;
import com.fms.sakar.sakir.service.RunnerService;
import com.fms.sakar.sakir.service.binance.BinanceMarketService;
import com.fms.sakar.sakir.util.DateUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fms.sakar.sakir.execution.ExecutionConstants.*;

@Slf4j
public class StrategyRunner implements Runnable {

    @Getter
    private final RunnerRequest runnerRequest;

    private final BinanceApiWebSocketClient webSocketClient;

    private final CandlestickCallbackProcessor callbackProcessor;

    private Closeable candlestickStream;


    public StrategyRunner(RunnerRequest runnerRequest, UUID taskId, RunnerService runnerService, BinanceMarketService marketService, PositionService positionService, BinanceApiWebSocketClient webSocketClient) {
        log.info("Initializing strategy runner");
        this.runnerRequest = runnerRequest;
        this.webSocketClient = webSocketClient;

        Map<String, Object> executionParams = initExecutionParams(marketService);

        this.callbackProcessor = new CandlestickCallbackProcessor(taskId, runnerService, positionService, executionParams);
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

        BarSeries barSeries = new BaseBarSeries(symbol + "_" + interval.getIntervalId());
        barSeries.setMaximumBarCount(BAR_SERIES_SIZE);

        List<Candlestick> candlestickBars = marketService.getCandlestickBars(symbol.toUpperCase(), interval, BAR_SERIES_SIZE);

        candlestickBars.forEach(c -> {
            ZonedDateTime closeTime = Instant.ofEpochMilli(c.getCloseTime()).atZone(DateUtils.getZoneId());
            barSeries.addBar(closeTime, Double.parseDouble(c.getOpen()),
                    Double.parseDouble(c.getHigh()), Double.parseDouble(c.getLow()),
                    Double.parseDouble(c.getClose()), Double.parseDouble(c.getVolume()));
        });

        Long lastOpenTime = candlestickBars.get(candlestickBars.size() - 1).getOpenTime();

        Map<String, Object> executionParams = new HashMap<>();

        executionParams.put(SYMBOL, symbol);
        executionParams.put(DURATION, duration);
        executionParams.put(STRATEGY_NAME, runnerRequest.getStrategyName());
        executionParams.put(BAR_SERIES, barSeries);
        executionParams.put(LAST_OPEN_TIME, lastOpenTime);

        return executionParams;
    }
}
