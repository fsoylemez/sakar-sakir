package com.fms.sakir.backtest.service;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.model.ExecutionRequest;
import com.fms.sakir.backtest.model.ExecutionResponse;
import com.fms.sakir.backtest.model.ExecutionResult;
import com.fms.sakir.backtest.model.StrategyPerformance;
import com.fms.sakir.backtest.strategy.factory.StrategyFactoryV2;
import com.fms.sakir.backtest.util.DateUtils;
import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.model.StrategyExecutionResponse;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class StrategyExecutorService {

    @Inject
    DataFetchService dataService;


    public List<StrategyExecutionResponse> execute(String strategy, CandlestickInterval interval, String ticker, String startDate, String endDate) {
        BarSeries series = new BaseBarSeries(ticker + "_series");
        List<Bar> bars = dataService.fetchData(ticker, interval, DateUtils.dateToMilli(startDate), DateUtils.dateToMilli(endDate));
        bars.forEach(series::addBar);

        List<SimpleStrategy> strategies = StrategyFactoryV2.getAll();
        List<StrategyExecutionResponse> response = new ArrayList<>(strategies.size());

        if (!"all".equals(strategy)) {
            strategies = strategies.stream().filter(a -> strategy.equals(a.getStrategyName())).collect(Collectors.toList());
        }

        strategies.forEach(ss -> response.add(ss.runStrategy(series, true)));

        return response;
    }

    public ExecutionResult execute(ExecutionRequest request, Boolean showPositions) {
        ExecutionResult result = new ExecutionResult();
        List<ExecutionResponse> responses = new ArrayList<>(request.getTickers().size());

        List<SimpleStrategy> strategies = StrategyFactoryV2.getAll(request.getStrategies());

        for (String ticker : request.getTickers()) {
            ExecutionResponse executionResponse = new ExecutionResponse();
            executionResponse.setTicker(ticker);

            BarSeries series = new BaseBarSeries(ticker + "_series");
            List<Bar> bars = dataService.fetchData(ticker, CandlestickInterval.valueOf(request.getInterval()), DateUtils.dateToMilli(request.getStartDate()), DateUtils.dateToMilli(request.getEndDate()));
            bars.forEach(series::addBar);

            List<StrategyExecutionResponse> strategyResponses = new ArrayList<>(strategies.size());

            strategies.forEach(ss -> strategyResponses.add(ss.runStrategy(series, showPositions)));

            executionResponse.setStrategyExecutionResponses(strategyResponses);
            String winnerStrategy = strategyResponses.stream().max(Comparator.comparing(StrategyExecutionResponse::getGrossReturn)).map(StrategyExecutionResponse::getStrategyName).orElse("NaN");
            executionResponse.setWinnerStrategy(winnerStrategy);
            responses.add(executionResponse);
        }

        result.setExecutionResponses(responses);

        Map<String, List<StrategyExecutionResponse>> responsesByStrategy = responses
                .stream()
                .map(ExecutionResponse::getStrategyExecutionResponses)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(StrategyExecutionResponse::getStrategyName));

        List<StrategyPerformance> performances = new ArrayList<>(strategies.size());
        responsesByStrategy.forEach((k,v)-> performances.add(new StrategyPerformance(k, v.stream().mapToDouble(StrategyExecutionResponse::getGrossReturn).sum())));

        result.setStrategyPerformances(performances.stream().sorted(Comparator.comparingDouble(StrategyPerformance::getTotalReturn).reversed()).collect(Collectors.toList()));

        return result;
    }
}
