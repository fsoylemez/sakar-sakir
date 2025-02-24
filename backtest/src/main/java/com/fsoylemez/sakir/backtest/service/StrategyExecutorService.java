package com.fsoylemez.sakir.backtest.service;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fsoylemez.sakir.backtest.db.couchdb.CouchDbService;
import com.fsoylemez.sakir.backtest.enums.TiingoInterval;
import com.fsoylemez.sakir.backtest.mapper.CandleStickMapper;
import com.fsoylemez.sakir.backtest.mapper.OhlcMapper;
import com.fsoylemez.sakir.backtest.model.*;
import com.fsoylemez.sakir.backtest.model.*;
import com.fsoylemez.sakir.backtest.model.tiingo.OhlcData;
import com.fsoylemez.sakir.backtest.util.DateUtils;
import com.fsoylemez.sakir.backtest.util.StringUtils;
import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import com.fsoylemez.sakir.strategy.factory.StrategyFactory;
import com.fsoylemez.sakir.strategy.model.PositionSummary;
import com.fsoylemez.sakir.strategy.model.StrategyExecutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class StrategyExecutorService {

    @Inject
    BinanceDataFetchService dataService;

    @Inject
    CouchDbService dbService;

    @Inject
    CandleStickMapper mapper;

    @Inject
    OhlcMapper ohlcMapper;


    public List<StrategyExecutionResponse> executeLive(String strategy, CandlestickInterval interval, String ticker, String startDate, String endDate) {
        BarSeries series = new BaseBarSeries(ticker + "_series");
        List<Bar> bars = dataService.fetchData(ticker, interval, DateUtils.dateToMilli(startDate), DateUtils.dateToMilli(endDate));
        bars.forEach(series::addBar);

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        List<StrategyExecutionResponse> response = new ArrayList<>(strategies.size());

        if (!"all".equals(strategy)) {
            strategies = strategies.stream().filter(a -> strategy.equals(a.getStrategyName())).collect(Collectors.toList());
        }

        strategies.forEach(ss -> response.add(ss.runStrategy(series, false)));

        return response;
    }

    public List<StrategyExecutionResponse> executeDb(String strategy, CandlestickInterval interval, String ticker, String startDate, String endDate, Boolean hidePositions) {
        BarSeries series = new BaseBarSeries(ticker + "_series");
        List<Candle> candlesticks = dbService.read(ticker, interval, DateUtils.dateToMilli(startDate), DateUtils.dateToMilli(endDate));

        candlesticks.forEach(c -> {
            try {
                series.addBar(mapper.toBar(c, interval));

            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
                dbService.delete(ticker, interval, c);
            }
        });

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        List<StrategyExecutionResponse> response = new ArrayList<>(strategies.size());

        if (!"all".equals(strategy)) {
            strategies = strategies.stream().filter(a -> strategy.equals(a.getStrategyName())).collect(Collectors.toList());
        }

        strategies.forEach(ss -> {
            StrategyExecutionResponse strategyExecutionResponse;
            StrategyPerformance performance = dbService.getPerformance(ss.getStrategyName(), ticker, interval, startDate, endDate);
            if (performance != null && Boolean.TRUE.equals(hidePositions)) {
                strategyExecutionResponse = StrategyExecutionResponse.builder()
                        .strategyName(performance.getStrategyName())
                        .positionCount(performance.getPositionCount())
                        .grossReturn(performance.getTotalReturn())
                        .build();
            } else {
                strategyExecutionResponse = ss.runStrategy(series, false);
            }
            response.add(strategyExecutionResponse);
        });

        savePerformances(response, interval, ticker, startDate, endDate);

        return response;
    }

    public List<StrategyExecutionResponse> executeDbFx(String strategy, CandlestickInterval interval, String ticker, String startDate, String endDate, Boolean hidePositions) {
        BarSeries series = new BaseBarSeries(ticker + "_series");
        List<OhlcData> candlesticks = dbService.readOhlc(ticker, interval, DateUtils.dateToMilli(startDate), DateUtils.dateToMilli(endDate));
        candlesticks.forEach(c-> {
            try {
                series.addBar(ohlcMapper.toBar(c, TiingoInterval.getByCandlestickInterval(interval)));
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
                dbService.delete(ticker, interval, c);
            }
        });

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        List<StrategyExecutionResponse> response = new ArrayList<>(strategies.size());

        if (!"all".equals(strategy)) {
            strategies = strategies.stream().filter(a -> strategy.equals(a.getStrategyName())).collect(Collectors.toList());
        }

        strategies.forEach(ss -> {
            StrategyExecutionResponse strategyExecutionResponse = null;
            StrategyPerformance performance = dbService.getPerformance(ss.getStrategyName(), ticker, interval, startDate, endDate);
            if (performance != null && Boolean.TRUE.equals(hidePositions)) {
                strategyExecutionResponse = StrategyExecutionResponse.builder()
                        .strategyName(performance.getStrategyName())
                        .positionCount(performance.getPositionCount())
                        .grossReturn(performance.getTotalReturn())
                        .build();
            } else {
                try {
                    strategyExecutionResponse = ss.runStrategy(series, false);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            if (strategyExecutionResponse != null) {
                response.add(strategyExecutionResponse);
            }
        });

        savePerformances(response, interval, ticker, startDate, endDate);

        return response;
    }

    public ExecutionResult executeLive(ExecutionRequest request, Boolean hidePositions) {
        ExecutionResult result = new ExecutionResult();
        List<ExecutionResponse> responses = new ArrayList<>(request.getTickers().size());

        List<SimpleStrategy> strategies = StrategyFactory.getStrategies(request.getStrategies());

        for (String ticker : request.getTickers()) {
            ExecutionResponse executionResponse = new ExecutionResponse();
            executionResponse.setTicker(ticker);

            BarSeries series = new BaseBarSeries(ticker + "_series");
            List<Bar> bars = dataService.fetchData(ticker, CandlestickInterval.valueOf(request.getInterval()), DateUtils.dateToMilli(request.getStartDate()), DateUtils.dateToMilli(request.getEndDate()));
            bars.forEach(series::addBar);

            List<StrategyExecutionResponse> strategyResponses = new ArrayList<>(strategies.size());

            strategies.forEach(ss -> strategyResponses.add(ss.runStrategy(series, hidePositions)));

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

        List<StrategyPerformanceSummary> performances = new ArrayList<>(strategies.size());
        responsesByStrategy.forEach((k,v)-> performances.add(new StrategyPerformanceSummary(k, v.stream().mapToDouble(StrategyExecutionResponse::getGrossReturn).sum())));

        result.setStrategyPerformances(performances.stream().sorted(Comparator.comparingDouble(StrategyPerformanceSummary::getTotalReturn).reversed()).collect(Collectors.toList()));

        return result;
    }

    public ExecutionResult executeDb(ExecutionRequest request, Boolean showPositions) {
        ExecutionResult result = new ExecutionResult();
        CandlestickInterval interval = CandlestickInterval.valueOf(request.getInterval());

        List<ExecutionResponse> responses = new ArrayList<>(request.getTickers().size());

        List<SimpleStrategy> strategies = StrategyFactory.getStrategies(request.getStrategies());

        for (String ticker : request.getTickers()) {
            ExecutionResponse executionResponse = new ExecutionResponse();
            executionResponse.setTicker(ticker);

            BarSeries series = new BaseBarSeries(ticker + "_series");
            AtomicReference<List<Candle>> candlesticks = new AtomicReference<>();

            List<StrategyExecutionResponse> strategyResponses = new ArrayList<>(strategies.size());

            strategies.forEach(ss -> {
                StrategyExecutionResponse strategyExecutionResponse;
                StrategyPerformance performance = dbService.getPerformance(ss.getStrategyName(), ticker, interval, request.getStartDate(), request.getEndDate());
                if (performance != null) {
                    strategyExecutionResponse = StrategyExecutionResponse.builder()
                            .strategyName(performance.getStrategyName())
                            .positionCount(performance.getPositionCount())
                            .grossReturn(performance.getTotalReturn())
                            .build();
                } else {
                    if (candlesticks.get() == null || candlesticks.get().isEmpty()) {
                        candlesticks.set(dbService.read(ticker, interval, DateUtils.dateToMilli(request.getStartDate()), DateUtils.dateToMilli(request.getEndDate())));
                        candlesticks.get().forEach(c-> series.addBar(mapper.toBar(c, interval)));
                    }
                    strategyExecutionResponse = ss.runStrategy(series, false);
                }
                strategyResponses.add(strategyExecutionResponse);
            });

            savePerformances(strategyResponses, interval, ticker, request.getStartDate(), request.getEndDate());

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

        List<StrategyPerformanceSummary> performances = new ArrayList<>(strategies.size());
        responsesByStrategy.forEach((k,v)-> performances.add(new StrategyPerformanceSummary(k, v.stream().mapToDouble(StrategyExecutionResponse::getGrossReturn).sum())));

        result.setStrategyPerformances(performances.stream().sorted(Comparator.comparingDouble(StrategyPerformanceSummary::getTotalReturn).reversed()).collect(Collectors.toList()));

        log.info("Execution DONE!");

        return result;
    }

    private void savePerformances(List<StrategyExecutionResponse> response, CandlestickInterval interval, String ticker, String startDate, String endDate) {
        if(response == null || response.isEmpty())
            return;
        List<StrategyPerformance> strategyPerformances = response.stream().map(ser -> {
            StrategyPerformance perf = new StrategyPerformance();
            perf.set_id(StringUtils.buildStrategyPerfId(ser.getStrategyName(), ticker, interval.getIntervalId(), startDate, endDate));
            perf.setStrategyName(ser.getStrategyName());
            perf.setTotalReturn(ser.getGrossReturn());
            perf.setPositionCount(ser.getPositionCount());
            perf.setSymbol(ticker);
            perf.setInterval(interval.getIntervalId());
            perf.setStartDate(startDate);
            perf.setEndDate(endDate);

            if (ser.getPositionCount() > 0 && ser.getPositions() != null) {
                long winningCount = ser.getPositions().stream().filter(PositionSummary::getWinning).count();
                perf.setWinningRate((double) winningCount / ser.getPositionCount());
            }

            return perf;
        }).collect(Collectors.toList());

        dbService.writePerformances(strategyPerformances);
    }
}
