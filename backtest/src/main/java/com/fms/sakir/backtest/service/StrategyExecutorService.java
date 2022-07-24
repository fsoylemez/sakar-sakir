package com.fms.sakir.backtest.service;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.model.ExecutionRequest;
import com.fms.sakir.backtest.model.ExecutionResponse;
import com.fms.sakir.backtest.util.DateUtils;
import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.factory.StrategyFactory;
import com.fms.sakir.strategy.model.StrategyExecutionResponse;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StrategyExecutorService {

    @Inject
    DataFetchService dataService;


    public List<StrategyExecutionResponse> execute(String strategy, CandlestickInterval interval, String ticker, String startDate, String endDate) {
        BarSeries series = new BaseBarSeries(ticker + "_series");
        List<Bar> bars = dataService.fetchData(ticker, interval, DateUtils.dateToMilli(startDate), DateUtils.dateToMilli(endDate));
        bars.forEach(series::addBar);

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        List<StrategyExecutionResponse> response = new ArrayList<>(strategies.size());

        if (!"all".equals(strategy)) {
            strategies = strategies.stream().filter(a -> strategy.equals(a.getStrategyName())).collect(Collectors.toList());
        }

        strategies.forEach(ss -> response.add(ss.runStrategy(series, true)));

        return response;
    }

    public List<ExecutionResponse> execute(ExecutionRequest request, Boolean showPositions) {

        List<ExecutionResponse> response = new ArrayList<>(request.getTickers().size());

        for (String ticker : request.getTickers()) {
            ExecutionResponse executionResponse = new ExecutionResponse();
            executionResponse.setTicker(ticker);

            BarSeries series = new BaseBarSeries(ticker + "_series");
            List<Bar> bars = dataService.fetchData(ticker, CandlestickInterval.valueOf(request.getInterval()), DateUtils.dateToMilli(request.getStartDate()), DateUtils.dateToMilli(request.getEndDate()));
            bars.forEach(series::addBar);

            List<SimpleStrategy> strategies = StrategyFactory.getStrategies(request.getStrategies());
            List<StrategyExecutionResponse> strategyResponses = new ArrayList<>(strategies.size());

            strategies.forEach(ss -> strategyResponses.add(ss.runStrategy(series, showPositions)));

            executionResponse.setStrategyExecutionResponses(strategyResponses);
            response.add(executionResponse);
        }

        return response;
    }
}
