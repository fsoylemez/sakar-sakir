package com.fsoylemez.sakar.sakir.service;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.fsoylemez.sakar.sakir.model.runner.RunnerRequest;
import com.fsoylemez.sakar.sakir.model.runner.RunnerResponse;
import com.fsoylemez.sakar.sakir.runner.StrategyRunner;
import com.fsoylemez.sakar.sakir.service.binance.BinanceMarketService;
import com.fsoylemez.sakar.sakir.service.binance.v2.BinancePositionServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class RunnerService {

    Map<UUID, StrategyRunner> runningTasks = new HashMap<>();

    @Inject
    ManagedExecutor executor;

    @Inject
    BinanceMarketService marketService;

    @Inject
    BinancePositionServiceV2 binancePositionService;

    @Inject
    BinanceApiWebSocketClient webSocketClient;

    public UUID newRunner(RunnerRequest runnerRequest) {
        log.info("Starting new runner with symbol {} chart {} and strategy {}", runnerRequest.getSymbol(), runnerRequest.getInterval().getIntervalId(), runnerRequest.getStrategyName());

        UUID taskId = UUID.randomUUID();

        StrategyRunner strategyRunner = new StrategyRunner(runnerRequest,taskId, this, marketService, binancePositionService, webSocketClient);
        executor.runAsync(strategyRunner);

        runningTasks.put(taskId, strategyRunner);

        return taskId;
    }

    public void stopTask(UUID taskId) throws IOException {
        StrategyRunner runner = runningTasks.get(taskId);
        if (runner == null) {
            log.error("No such runner found");
        } else {
            log.info("Stopping runner");

            runner.stop();
            runningTasks.remove(taskId);
        }
    }

    public void reconnect(UUID taskId) {
        StrategyRunner runner = runningTasks.get(taskId);
        executor.runAsync(runner);
    }

    public List<RunnerResponse> getRunning() {
        return runningTasks.entrySet().stream().map(e-> new RunnerResponse(e.getKey(), e.getValue().getRunnerRequest())).collect(Collectors.toList());
    }
}
