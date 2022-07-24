package com.fms.sakar.sakir.service;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.fms.sakar.sakir.model.runner.RunnerRequest;
import com.fms.sakar.sakir.runner.StrategyRunner;
import com.fms.sakar.sakir.service.binance.BinanceMarketService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class RunnerService {

    Map<UUID, StrategyRunner> runningTasks = new HashMap<>();

    @Inject
    ManagedExecutor executor;

    @Inject
    BinanceMarketService marketService;

    @Inject
    PositionService positionService;

    @Inject
    BinanceApiWebSocketClient webSocketClient;

    public UUID newRunner(RunnerRequest runnerRequest) {
        log.info("Starting new runner with symbol {} chart {}", runnerRequest.getSymbol(), runnerRequest.getInterval().getIntervalId());

        UUID taskId = UUID.randomUUID();

        StrategyRunner strategyRunner = new StrategyRunner(runnerRequest,taskId, this, marketService, positionService, webSocketClient);
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
}
