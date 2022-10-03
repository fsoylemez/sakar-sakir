package com.fms.sakir.backtest.service;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.enums.TiingoMarket;
import com.fms.sakir.backtest.model.*;
import com.fms.sakir.backtest.util.BackTestConstants;
import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.factory.StrategyFactory;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class StrategyPerformanceService {

    @Inject
    CouchDbService dbService;

    @Inject
    StrategyExecutorService executorService;


    public PerformanceStats getPerformanceStatistics(String startDate, String endDate) {
        PerformanceStats stats = new PerformanceStats();

        List<StrategyPerformance> performances = dbService.getPerformanceStatistics(startDate, endDate);

        //best 5 total return
        List<StrategyPerformance> bestTotalReturn = performances.stream()
                .sorted(Comparator.comparingDouble(StrategyPerformance::getTotalReturn).reversed())
                .collect(Collectors.toList())
                .subList(0, 5);
        stats.setBestTotalReturn(bestTotalReturn);

        //best 5 winning rate
        List<StrategyPerformance> bestWinningRate = performances.stream()
                .sorted(Comparator.comparingDouble(StrategyPerformance::getWinningRate).reversed())
                .collect(Collectors.toList())
                .subList(0, 5);
        stats.setBestWinningRate(bestWinningRate);

        //best 5 winning ratios (divide winning rate by position count)
        Comparator<StrategyPerformance> byWinningPerPosition =
                Comparator.comparingDouble(p -> p.getTotalReturn() / p.getPositionCount());

        List<StrategyPerformance> bestWinningPerPosition = performances.stream()
                .filter(p-> p.getPositionCount() > 0 && p.getTotalReturn() > 1)
                .sorted(byWinningPerPosition.reversed())
                .collect(Collectors.toList())
                .subList(0, 5);
        stats.setBestWinningRatio(bestWinningPerPosition);

        //best 5 with best worst performance
        Map<String, List<StrategyPerformance>> byStrategy = performances.stream()
                .collect(Collectors.groupingBy(StrategyPerformance::getStrategyName));

        for(List<StrategyPerformance> perfs : byStrategy.values()) {
            perfs.sort(Comparator.nullsLast(Comparator.comparingDouble(StrategyPerformance::getTotalReturn)));
        }

        List<StrategyPerformance> bestWorstPerformances = byStrategy.values().stream()
                .map(a -> a.get(0))
                .sorted(Comparator.comparingDouble(StrategyPerformance::getTotalReturn).reversed())
                .collect(Collectors.toList())
                .subList(0, 5);
        stats.setBestWorstPerformance(bestWorstPerformances);
        //best 5 average perf


        return stats;
    }

    public void sync(TiingoMarket market) {
        LocalDate now = LocalDate.now();

        int currentMonth = now.get(ChronoField.MONTH_OF_YEAR);

        for (int i=1; i<currentMonth; i++) {

            LocalDate monthStart = LocalDate.of(now.getYear(), i, 1);
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());

            String startDate = monthStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String endDate = monthEnd.format(DateTimeFormatter.ISO_LOCAL_DATE);

            if (TiingoMarket.STOCK.equals(market)) {
                syncForDateStock(startDate, endDate, "MONTHLY", monthStart.getMonth().name());
            } else if(TiingoMarket.FX.equals(market)) {
                syncForDateFx(startDate, endDate, "MONTHLY", monthStart.getMonth().name());
            } else {
                syncForDate(startDate, endDate, "MONTHLY", monthStart.getMonth().name());
            }
        }

        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
        LocalDate yearEnd = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        String startDate = yearStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDate = yearEnd.format(DateTimeFormatter.ISO_LOCAL_DATE);

        if (TiingoMarket.STOCK.equals(market)) {
            syncForDateStock(startDate, endDate, "YEARLY", String.valueOf(yearStart.getYear()));
        } else if(TiingoMarket.FX.equals(market)) {
            syncForDateFx(startDate, endDate, "YEARLY", String.valueOf(yearStart.getYear()));
        } else {
            syncForDate(startDate, endDate, "YEARLY", String.valueOf(yearStart.getYear()));
        }

        log.info("syncing COMPLETED");
    }

    public void syncForDate(String startDate, String endDate, String period, String periodValue) {
        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        CandlestickInterval[] intervals = BackTestConstants.INTERVALS;
        String[] tickers = BackTestConstants.TICKERS;

        log.info("Calculating period for {} {}", period, periodValue);

        strategies.forEach(s -> {
            for (String ticker : tickers) {
                for (CandlestickInterval interval : intervals) {
                    StrategyPerformance performance = dbService.getPerformance(s.getStrategyName(), ticker, interval, startDate, endDate);

                    if (performance != null) {
                        if (performance.getPeriod() == null) {
                            log.info("Calculating only period for {} {} {}", s.getStrategyName(), ticker, interval);
                            performance.setPeriod(period);
                            performance.setPeriodValue(periodValue);
                            dbService.updatePerformance(performance);
                        }
                    } else {
                        log.info("Calculating outcome for {} {} {}", s.getStrategyName(), ticker, interval);
                        executorService.executeDb(s.getStrategyName(), interval, ticker, startDate, endDate, true);
                    }
                }
            }
        });
    }

    public void syncForDateFx(String startDate, String endDate, String period, String periodValue) {
        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        CandlestickInterval[] intervals = {CandlestickInterval.FIVE_MINUTES};
        String[] tickers = BackTestConstants.FX_PAIRS;

        log.info("Calculating period for {} {}", period, periodValue);

        strategies.forEach(s -> {
            for (String ticker : tickers) {
                for (CandlestickInterval interval : intervals) {
                    StrategyPerformance performance = dbService.getPerformance(s.getStrategyName(), ticker, interval, startDate, endDate);

                    if (performance != null) {
                        if (performance.getPeriod() == null) {
                            log.info("Calculating only period for {} {} {}", s.getStrategyName(), ticker, interval);
                            performance.setPeriod(period);
                            performance.setPeriodValue(periodValue);
                            dbService.updatePerformance(performance);
                        }
                    } else {
                        log.info("Calculating outcome for {} {} {}", s.getStrategyName(), ticker, interval);
                        executorService.executeDbFx(s.getStrategyName(), interval, ticker, startDate, endDate, true);
                    }
                }
            }
        });
    }

    public void syncForDateStock(String startDate, String endDate, String period, String periodValue) {
        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();
        CandlestickInterval[] intervals = {CandlestickInterval.FIVE_MINUTES, CandlestickInterval.FIFTEEN_MINUTES};
        String[] tickers = BackTestConstants.STOCK_SYMBOLS;

        log.info("Calculating period for {} {}", period, periodValue);

        strategies.forEach(s -> {
            for (String ticker : tickers) {
                for (CandlestickInterval interval : intervals) {
                    StrategyPerformance performance = dbService.getPerformance(s.getStrategyName(), ticker, interval, startDate, endDate);

                    if (performance != null) {
                        if (performance.getPeriod() == null) {
                            log.info("Calculating only period for {} {} {}", s.getStrategyName(), ticker, interval);
                            performance.setPeriod(period);
                            performance.setPeriodValue(periodValue);
                            dbService.updatePerformance(performance);
                        }
                    } else {
                        log.info("Calculating outcome for {} {} {}", s.getStrategyName(), ticker, interval);
                        executorService.executeDbFx(s.getStrategyName(), interval, ticker, startDate, endDate, true);
                    }
                }
            }
        });
    }

    public void prepareSummary(boolean update) {

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();


        for (SimpleStrategy strategy : strategies) {
            PerformanceSummary summary = new PerformanceSummary();
            summary.set_id(strategy.getStrategyName());
            summary.setStrategyName(strategy.getStrategyName());

            PerformanceSummary existing = dbService.getPerformanceSummary(strategy.getStrategyName());
            if ( existing != null) {
                if (update) {
                    summary = existing;
                } else {
                    continue;
                }
            }

            List<StrategyPerformance> performances = dbService.getPerformanceByStrategy(strategy.getStrategyName());

            Optional<StrategyPerformance> best = getMaxReturn(performances, null);
            Optional<StrategyPerformance> worst = getMinReturn(performances, null);
            double avgReturn = getAverageReturn(performances, null);
            double avgPositionCount = getAveragePositionCount(performances, null);

            if (best.isPresent()) {
                StrategyPerformance bestPerf = best.get();
                summary.setBestPeriod(bestPerf.getPeriodValue());
                summary.setBestInterval(bestPerf.getInterval());
                summary.setBestReturn(bestPerf.getTotalReturn());
                summary.setBestSymbol(bestPerf.getSymbol());
            }

            if (worst.isPresent()) {
                StrategyPerformance worstPerf = worst.get();
                summary.setWorstPeriod(worstPerf.getPeriodValue());
                summary.setWorstInterval(worstPerf.getInterval());
                summary.setWorstReturn(worstPerf.getTotalReturn());
                summary.setWorstSymbol(worstPerf.getSymbol());
            }

            summary.setAverageReturn(avgReturn);
            summary.setAveragePositionCount(avgPositionCount);


            Optional<StrategyPerformance> best4h = getMaxReturn(performances, CandlestickInterval.FOUR_HOURLY);
            Optional<StrategyPerformance> worst4h = getMinReturn(performances, CandlestickInterval.FOUR_HOURLY);
            double avgReturn4h = getAverageReturn(performances, CandlestickInterval.FOUR_HOURLY);
            double avgPositionCount4h = getAveragePositionCount(performances, CandlestickInterval.FOUR_HOURLY);

            if (best4h.isPresent()) {
                StrategyPerformance best4hPerf = best4h.get();
                summary.setH4BestPeriod(best4hPerf.getPeriodValue());
                summary.setH4BestInterval(best4hPerf.getInterval());
                summary.setH4BestReturn(best4hPerf.getTotalReturn());
                summary.setH4BestSymbol(best4hPerf.getSymbol());
            }

            if (worst4h.isPresent()) {
                StrategyPerformance worst4hPerf = worst4h.get();
                summary.setH4WorstPeriod(worst4hPerf.getPeriodValue());
                summary.setH4WorstInterval(worst4hPerf.getInterval());
                summary.setH4WorstReturn(worst4hPerf.getTotalReturn());
                summary.setH4WorstSymbol(worst4hPerf.getSymbol());
            }

            summary.setH4AverageReturn(avgReturn4h);
            summary.setH4AveragePositionCount(avgPositionCount4h);



            Optional<StrategyPerformance> best2h = getMaxReturn(performances, CandlestickInterval.TWO_HOURLY);
            Optional<StrategyPerformance> worst2h = getMinReturn(performances, CandlestickInterval.TWO_HOURLY);
            double avgReturn2h = getAverageReturn(performances, CandlestickInterval.TWO_HOURLY);
            double avgPositionCount2h = getAveragePositionCount(performances, CandlestickInterval.TWO_HOURLY);

            if (best2h.isPresent()) {
                StrategyPerformance best2hPerf = best2h.get();
                summary.setH2BestPeriod(best2hPerf.getPeriodValue());
                summary.setH2BestInterval(best2hPerf.getInterval());
                summary.setH2BestReturn(best2hPerf.getTotalReturn());
                summary.setH2BestSymbol(best2hPerf.getSymbol());
            }

            if (worst2h.isPresent()) {
                StrategyPerformance worst2hPerf = worst2h.get();
                summary.setH2WorstPeriod(worst2hPerf.getPeriodValue());
                summary.setH2WorstInterval(worst2hPerf.getInterval());
                summary.setH2WorstReturn(worst2hPerf.getTotalReturn());
                summary.setH2WorstSymbol(worst2hPerf.getSymbol());
            }

            summary.setH2AverageReturn(avgReturn2h);
            summary.setH2AveragePositionCount(avgPositionCount2h);



            Optional<StrategyPerformance> best1h = getMaxReturn(performances, CandlestickInterval.HOURLY);
            Optional<StrategyPerformance> worst1h = getMinReturn(performances, CandlestickInterval.HOURLY);
            double avgReturn1h = getAverageReturn(performances, CandlestickInterval.HOURLY);
            double avgPositionCount1h = getAveragePositionCount(performances, CandlestickInterval.HOURLY);

            if (best1h.isPresent()) {
                StrategyPerformance best1hPerf = best1h.get();
                summary.setH1BestPeriod(best1hPerf.getPeriodValue());
                summary.setH1BestInterval(best1hPerf.getInterval());
                summary.setH1BestReturn(best1hPerf.getTotalReturn());
                summary.setH1BestSymbol(best1hPerf.getSymbol());
            }

            if (worst1h.isPresent()) {
                StrategyPerformance worst1hPerf = worst1h.get();
                summary.setH1WorstPeriod(worst1hPerf.getPeriodValue());
                summary.setH1WorstInterval(worst1hPerf.getInterval());
                summary.setH1WorstReturn(worst1hPerf.getTotalReturn());
                summary.setH1WorstSymbol(worst1hPerf.getSymbol());
            }

            summary.setH1AverageReturn(avgReturn1h);
            summary.setH1AveragePositionCount(avgPositionCount1h);



            Optional<StrategyPerformance> best30m = getMaxReturn(performances, CandlestickInterval.HALF_HOURLY);
            Optional<StrategyPerformance> worst30m = getMinReturn(performances, CandlestickInterval.HALF_HOURLY);
            double avgReturn30m = getAverageReturn(performances, CandlestickInterval.HALF_HOURLY);
            double avgPositionCount30m = getAveragePositionCount(performances, CandlestickInterval.HALF_HOURLY);

            if (best30m.isPresent()) {
                StrategyPerformance best30mPerf = best30m.get();
                summary.setM30BestPeriod(best30mPerf.getPeriodValue());
                summary.setM30BestInterval(best30mPerf.getInterval());
                summary.setM30BestReturn(best30mPerf.getTotalReturn());
                summary.setM30BestSymbol(best30mPerf.getSymbol());
            }

            if (worst30m.isPresent()) {
                StrategyPerformance worst30mPerf = worst30m.get();
                summary.setM30WorstPeriod(worst30mPerf.getPeriodValue());
                summary.setM30WorstInterval(worst30mPerf.getInterval());
                summary.setM30WorstReturn(worst30mPerf.getTotalReturn());
                summary.setM30WorstSymbol(worst30mPerf.getSymbol());
            }

            summary.setM30AverageReturn(avgReturn30m);
            summary.setM30AveragePositionCount(avgPositionCount30m);



            Optional<StrategyPerformance> best15m = getMaxReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
            Optional<StrategyPerformance> worst15m = getMinReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
            double avgReturn15m = getAverageReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
            double avgPositionCount15m = getAveragePositionCount(performances, CandlestickInterval.FIFTEEN_MINUTES);

            if (best15m.isPresent()) {
                StrategyPerformance best15mPerf = best15m.get();
                summary.setM15BestPeriod(best15mPerf.getPeriodValue());
                summary.setM15BestInterval(best15mPerf.getInterval());
                summary.setM15BestReturn(best15mPerf.getTotalReturn());
                summary.setM15BestSymbol(best15mPerf.getSymbol());
            }

            if (worst15m.isPresent()) {
                StrategyPerformance worst15mPerf = worst15m.get();
                summary.setM15WorstPeriod(worst15mPerf.getPeriodValue());
                summary.setM15WorstInterval(worst15mPerf.getInterval());
                summary.setM15WorstReturn(worst15mPerf.getTotalReturn());
                summary.setM15WorstSymbol(worst15mPerf.getSymbol());
            }

            summary.setM15AverageReturn(avgReturn15m);
            summary.setM15AveragePositionCount(avgPositionCount15m);



            Optional<StrategyPerformance> best5m = getMaxReturn(performances, CandlestickInterval.FIVE_MINUTES);
            Optional<StrategyPerformance> worst5m = getMinReturn(performances, CandlestickInterval.FIVE_MINUTES);
            double avgReturn5m = getAverageReturn(performances, CandlestickInterval.FIVE_MINUTES);
            double avgPositionCount5m = getAveragePositionCount(performances, CandlestickInterval.FIVE_MINUTES);

            if (best5m.isPresent()) {
                StrategyPerformance best5mPerf = best5m.get();
                summary.setM5BestPeriod(best5mPerf.getPeriodValue());
                summary.setM5BestInterval(best5mPerf.getInterval());
                summary.setM5BestReturn(best5mPerf.getTotalReturn());
                summary.setM5BestSymbol(best5mPerf.getSymbol());
            }

            if (worst5m.isPresent()) {
                StrategyPerformance worst5mPerf = worst5m.get();
                summary.setM5WorstPeriod(worst5mPerf.getPeriodValue());
                summary.setM5WorstInterval(worst5mPerf.getInterval());
                summary.setM5WorstReturn(worst5mPerf.getTotalReturn());
                summary.setM5WorstSymbol(worst5mPerf.getSymbol());
            }

            summary.setM5AverageReturn(avgReturn5m);
            summary.setM5AveragePositionCount(avgPositionCount5m);


            if (summary.get_rev() != null) {
                dbService.updateSummary(summary);
            } else {
                dbService.saveSummary(summary);
            }
        }

    }

    public Optional<StrategyPerformance> getMaxReturn(List<StrategyPerformance> performances, CandlestickInterval interval) {
        if (performances == null || performances.isEmpty())
            return Optional.empty();

        return performances.stream().filter(p -> p.getPositionCount() > 0 && (interval == null || interval.getIntervalId().equals(p.getInterval()))).max(Comparator.comparingDouble(StrategyPerformance::getTotalReturn));
    }

    public Optional<StrategyPerformance> getMinReturn(List<StrategyPerformance> performances, CandlestickInterval interval) {
        if (performances == null || performances.isEmpty())
            return Optional.empty();

        return performances.stream().filter(p -> p.getPositionCount() > 0 && (interval == null || interval.getIntervalId().equals(p.getInterval()))).min(Comparator.comparingDouble(StrategyPerformance::getTotalReturn));
    }

    public double getAverageReturn(List<StrategyPerformance> performances, CandlestickInterval interval) {
        if (performances == null || performances.isEmpty())
            return 0;

        return performances.stream().filter(p -> p.getPositionCount() > 0 && (interval == null || interval.getIntervalId().equals(p.getInterval()))).mapToDouble(StrategyPerformance::getTotalReturn).average().orElse(Double.NaN);
    }

    public double getAveragePositionCount(List<StrategyPerformance> performances, CandlestickInterval interval) {
        if (performances == null || performances.isEmpty())
            return 0;

        return performances.stream().filter(p -> p.getPositionCount() > 0 && (interval == null || interval.getIntervalId().equals(p.getInterval()))).mapToDouble(StrategyPerformance::getPositionCount).average().orElse(Double.NaN);
    }

    public void prepareSummaryFx(boolean update) {

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();


        for (SimpleStrategy strategy : strategies) {
            if (List.of("Cmf", "AroonCmf", "AroonComboShort").contains(strategy.getStrategyName())) {
                continue;
            }
            PerformanceSummaryFx summary = new PerformanceSummaryFx();
            summary.set_id(strategy.getStrategyName());
            summary.setStrategyName(strategy.getStrategyName());

            PerformanceSummaryFx existing = dbService.getPerformanceSummaryFx(strategy.getStrategyName());
            if ( existing != null) {
                if (update) {
                    summary = existing;
                } else {
                    continue;
                }
            }

            List<StrategyPerformance> performances = dbService.getPerformanceByStrategyFx(strategy.getStrategyName());

            Optional<StrategyPerformance> best = getMaxReturn(performances, null);
            Optional<StrategyPerformance> worst = getMinReturn(performances, null);
            double avgReturn = getAverageReturn(performances, null);
            double avgPositionCount = getAveragePositionCount(performances, null);

            if (best.isPresent()) {
                StrategyPerformance bestPerf = best.get();
                summary.setBestPeriod(bestPerf.getPeriodValue());
                summary.setBestInterval(bestPerf.getInterval());
                summary.setBestReturn(bestPerf.getTotalReturn());
                summary.setBestSymbol(bestPerf.getSymbol());
            }

            if (worst.isPresent()) {
                StrategyPerformance worstPerf = worst.get();
                summary.setWorstPeriod(worstPerf.getPeriodValue());
                summary.setWorstInterval(worstPerf.getInterval());
                summary.setWorstReturn(worstPerf.getTotalReturn());
                summary.setWorstSymbol(worstPerf.getSymbol());
            }

            summary.setAverageReturn(avgReturn);
            summary.setAveragePositionCount(avgPositionCount);


            Optional<StrategyPerformance> best15m = getMaxReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
            Optional<StrategyPerformance> worst15m = getMinReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
            double avgReturn15m = getAverageReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
            double avgPositionCount15m = getAveragePositionCount(performances, CandlestickInterval.FIFTEEN_MINUTES);

            if (best15m.isPresent()) {
                StrategyPerformance best15mPerf = best15m.get();
                summary.setM15BestPeriod(best15mPerf.getPeriodValue());
                summary.setM15BestInterval(best15mPerf.getInterval());
                summary.setM15BestReturn(best15mPerf.getTotalReturn());
                summary.setM15BestSymbol(best15mPerf.getSymbol());
            }

            if (worst15m.isPresent()) {
                StrategyPerformance worst15mPerf = worst15m.get();
                summary.setM15WorstPeriod(worst15mPerf.getPeriodValue());
                summary.setM15WorstInterval(worst15mPerf.getInterval());
                summary.setM15WorstReturn(worst15mPerf.getTotalReturn());
                summary.setM15WorstSymbol(worst15mPerf.getSymbol());
            }

            summary.setM15AverageReturn(avgReturn15m);
            summary.setM15AveragePositionCount(avgPositionCount15m);



            Optional<StrategyPerformance> best5m = getMaxReturn(performances, CandlestickInterval.FIVE_MINUTES);
            Optional<StrategyPerformance> worst5m = getMinReturn(performances, CandlestickInterval.FIVE_MINUTES);
            double avgReturn5m = getAverageReturn(performances, CandlestickInterval.FIVE_MINUTES);
            double avgPositionCount5m = getAveragePositionCount(performances, CandlestickInterval.FIVE_MINUTES);

            if (best5m.isPresent()) {
                StrategyPerformance best5mPerf = best5m.get();
                summary.setM5BestPeriod(best5mPerf.getPeriodValue());
                summary.setM5BestInterval(best5mPerf.getInterval());
                summary.setM5BestReturn(best5mPerf.getTotalReturn());
                summary.setM5BestSymbol(best5mPerf.getSymbol());
            }

            if (worst5m.isPresent()) {
                StrategyPerformance worst5mPerf = worst5m.get();
                summary.setM5WorstPeriod(worst5mPerf.getPeriodValue());
                summary.setM5WorstInterval(worst5mPerf.getInterval());
                summary.setM5WorstReturn(worst5mPerf.getTotalReturn());
                summary.setM5WorstSymbol(worst5mPerf.getSymbol());
            }

            summary.setM5AverageReturn(avgReturn5m);
            summary.setM5AveragePositionCount(avgPositionCount5m);


            if (summary.get_rev() != null) {
                dbService.updateSummaryFx(summary);
            } else {
                dbService.saveSummaryFx(summary);
            }
        }

    }

    public void prepareSummaryFxByPair(boolean update) {

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();


        for (SimpleStrategy strategy : strategies) {
            if (List.of("Cmf", "AroonCmf", "AroonComboShort").contains(strategy.getStrategyName())) {
                continue;
            }
            for (String symbol : BackTestConstants.FX_PAIRS) {
                PerformanceSummaryByPairFx summary = new PerformanceSummaryByPairFx();
                summary.setStrategyName(strategy.getStrategyName());
                summary.setSymbol(symbol);
                summary.set_id(String.join("_", strategy.getStrategyName(), symbol));

                PerformanceSummaryByPairFx existing = dbService.getPerformanceSummaryByPairFx(strategy.getStrategyName(), symbol);
                if (existing != null) {
                    if (update) {
                        summary = existing;
                    } else {
                        continue;
                    }
                }

                List<StrategyPerformance> performances = dbService.getPerformanceByStrategyAndPair("aaa_strategy_performance_fx", strategy.getStrategyName(), symbol);

                Optional<StrategyPerformance> best = getMaxReturn(performances, null);
                Optional<StrategyPerformance> worst = getMinReturn(performances, null);
                double avgReturn = getAverageReturn(performances, null);
                double avgPositionCount = getAveragePositionCount(performances, null);

                if (best.isPresent()) {
                    StrategyPerformance bestPerf = best.get();
                    summary.setBestPeriod(bestPerf.getPeriodValue());
                    summary.setBestInterval(bestPerf.getInterval());
                    summary.setBestReturn(bestPerf.getTotalReturn());
                }

                if (worst.isPresent()) {
                    StrategyPerformance worstPerf = worst.get();
                    summary.setWorstPeriod(worstPerf.getPeriodValue());
                    summary.setWorstInterval(worstPerf.getInterval());
                    summary.setWorstReturn(worstPerf.getTotalReturn());
                }

                summary.setAverageReturn(avgReturn);
                summary.setAveragePositionCount(avgPositionCount);


                Optional<StrategyPerformance> best15m = getMaxReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                Optional<StrategyPerformance> worst15m = getMinReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                double avgReturn15m = getAverageReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                double avgPositionCount15m = getAveragePositionCount(performances, CandlestickInterval.FIFTEEN_MINUTES);

                if (best15m.isPresent()) {
                    StrategyPerformance best15mPerf = best15m.get();
                    summary.setM15BestPeriod(best15mPerf.getPeriodValue());
                    summary.setM15BestInterval(best15mPerf.getInterval());
                    summary.setM15BestReturn(best15mPerf.getTotalReturn());
                }

                if (worst15m.isPresent()) {
                    StrategyPerformance worst15mPerf = worst15m.get();
                    summary.setM15WorstPeriod(worst15mPerf.getPeriodValue());
                    summary.setM15WorstInterval(worst15mPerf.getInterval());
                    summary.setM15WorstReturn(worst15mPerf.getTotalReturn());
                }

                summary.setM15AverageReturn(avgReturn15m);
                summary.setM15AveragePositionCount(avgPositionCount15m);


                Optional<StrategyPerformance> best5m = getMaxReturn(performances, CandlestickInterval.FIVE_MINUTES);
                Optional<StrategyPerformance> worst5m = getMinReturn(performances, CandlestickInterval.FIVE_MINUTES);
                double avgReturn5m = getAverageReturn(performances, CandlestickInterval.FIVE_MINUTES);
                double avgPositionCount5m = getAveragePositionCount(performances, CandlestickInterval.FIVE_MINUTES);

                if (best5m.isPresent()) {
                    StrategyPerformance best5mPerf = best5m.get();
                    summary.setM5BestPeriod(best5mPerf.getPeriodValue());
                    summary.setM5BestInterval(best5mPerf.getInterval());
                    summary.setM5BestReturn(best5mPerf.getTotalReturn());
                }

                if (worst5m.isPresent()) {
                    StrategyPerformance worst5mPerf = worst5m.get();
                    summary.setM5WorstPeriod(worst5mPerf.getPeriodValue());
                    summary.setM5WorstInterval(worst5mPerf.getInterval());
                    summary.setM5WorstReturn(worst5mPerf.getTotalReturn());
                }

                summary.setM5AverageReturn(avgReturn5m);
                summary.setM5AveragePositionCount(avgPositionCount5m);


                if (summary.get_rev() != null) {
                    dbService.updateSummaryByPairFx(summary);
                } else {
                    dbService.saveSummaryByPairFx(summary);
                }
            }
        }
    }

    public void prepareSummaryByPair(boolean update) {

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();


        for (SimpleStrategy strategy : strategies) {

            for (String symbol : BackTestConstants.TICKERS) {
                PerformanceSummaryByPair summary = new PerformanceSummaryByPair();
                summary.setStrategyName(strategy.getStrategyName());
                summary.setSymbol(symbol);
                summary.set_id(String.join("_", strategy.getStrategyName(), symbol));

                PerformanceSummaryByPair existing = dbService.getPerformanceSummaryByPair(strategy.getStrategyName(), symbol);
                if (existing != null) {
                    if (update) {
                        summary = existing;
                    } else {
                        continue;
                    }
                }

                List<StrategyPerformance> performances = dbService.getPerformanceByStrategyAndPair("strategy_performance", strategy.getStrategyName(), symbol);

                Optional<StrategyPerformance> best = getMaxReturn(performances, null);
                Optional<StrategyPerformance> worst = getMinReturn(performances, null);
                double avgReturn = getAverageReturn(performances, null);
                double avgPositionCount = getAveragePositionCount(performances, null);

                if (best.isPresent()) {
                    StrategyPerformance bestPerf = best.get();
                    summary.setBestPeriod(bestPerf.getPeriodValue());
                    summary.setBestInterval(bestPerf.getInterval());
                    summary.setBestReturn(bestPerf.getTotalReturn());
                }

                if (worst.isPresent()) {
                    StrategyPerformance worstPerf = worst.get();
                    summary.setWorstPeriod(worstPerf.getPeriodValue());
                    summary.setWorstInterval(worstPerf.getInterval());
                    summary.setWorstReturn(worstPerf.getTotalReturn());
                }

                summary.setAverageReturn(avgReturn);
                summary.setAveragePositionCount(avgPositionCount);


                Optional<StrategyPerformance> best4h = getMaxReturn(performances, CandlestickInterval.FOUR_HOURLY);
                Optional<StrategyPerformance> worst4h = getMinReturn(performances, CandlestickInterval.FOUR_HOURLY);
                double avgReturn4h = getAverageReturn(performances, CandlestickInterval.FOUR_HOURLY);
                double avgPositionCount4h = getAveragePositionCount(performances, CandlestickInterval.FOUR_HOURLY);

                if (best4h.isPresent()) {
                    StrategyPerformance best4hPerf = best4h.get();
                    summary.setH4BestPeriod(best4hPerf.getPeriodValue());
                    summary.setH4BestInterval(best4hPerf.getInterval());
                    summary.setH4BestReturn(best4hPerf.getTotalReturn());
                }

                if (worst4h.isPresent()) {
                    StrategyPerformance worst4hPerf = worst4h.get();
                    summary.setH4WorstPeriod(worst4hPerf.getPeriodValue());
                    summary.setH4WorstInterval(worst4hPerf.getInterval());
                    summary.setH4WorstReturn(worst4hPerf.getTotalReturn());
                }

                summary.setH4AverageReturn(avgReturn4h);
                summary.setH4AveragePositionCount(avgPositionCount4h);



                Optional<StrategyPerformance> best2h = getMaxReturn(performances, CandlestickInterval.TWO_HOURLY);
                Optional<StrategyPerformance> worst2h = getMinReturn(performances, CandlestickInterval.TWO_HOURLY);
                double avgReturn2h = getAverageReturn(performances, CandlestickInterval.TWO_HOURLY);
                double avgPositionCount2h = getAveragePositionCount(performances, CandlestickInterval.TWO_HOURLY);

                if (best2h.isPresent()) {
                    StrategyPerformance best2hPerf = best2h.get();
                    summary.setH2BestPeriod(best2hPerf.getPeriodValue());
                    summary.setH2BestInterval(best2hPerf.getInterval());
                    summary.setH2BestReturn(best2hPerf.getTotalReturn());
                }

                if (worst2h.isPresent()) {
                    StrategyPerformance worst2hPerf = worst2h.get();
                    summary.setH2WorstPeriod(worst2hPerf.getPeriodValue());
                    summary.setH2WorstInterval(worst2hPerf.getInterval());
                    summary.setH2WorstReturn(worst2hPerf.getTotalReturn());
                }

                summary.setH2AverageReturn(avgReturn2h);
                summary.setH2AveragePositionCount(avgPositionCount2h);



                Optional<StrategyPerformance> best1h = getMaxReturn(performances, CandlestickInterval.HOURLY);
                Optional<StrategyPerformance> worst1h = getMinReturn(performances, CandlestickInterval.HOURLY);
                double avgReturn1h = getAverageReturn(performances, CandlestickInterval.HOURLY);
                double avgPositionCount1h = getAveragePositionCount(performances, CandlestickInterval.HOURLY);

                if (best1h.isPresent()) {
                    StrategyPerformance best1hPerf = best1h.get();
                    summary.setH1BestPeriod(best1hPerf.getPeriodValue());
                    summary.setH1BestInterval(best1hPerf.getInterval());
                    summary.setH1BestReturn(best1hPerf.getTotalReturn());
                }

                if (worst1h.isPresent()) {
                    StrategyPerformance worst1hPerf = worst1h.get();
                    summary.setH1WorstPeriod(worst1hPerf.getPeriodValue());
                    summary.setH1WorstInterval(worst1hPerf.getInterval());
                    summary.setH1WorstReturn(worst1hPerf.getTotalReturn());
                }

                summary.setH1AverageReturn(avgReturn1h);
                summary.setH1AveragePositionCount(avgPositionCount1h);



                Optional<StrategyPerformance> best30m = getMaxReturn(performances, CandlestickInterval.HALF_HOURLY);
                Optional<StrategyPerformance> worst30m = getMinReturn(performances, CandlestickInterval.HALF_HOURLY);
                double avgReturn30m = getAverageReturn(performances, CandlestickInterval.HALF_HOURLY);
                double avgPositionCount30m = getAveragePositionCount(performances, CandlestickInterval.HALF_HOURLY);

                if (best30m.isPresent()) {
                    StrategyPerformance best30mPerf = best30m.get();
                    summary.setM30BestPeriod(best30mPerf.getPeriodValue());
                    summary.setM30BestInterval(best30mPerf.getInterval());
                    summary.setM30BestReturn(best30mPerf.getTotalReturn());
                }

                if (worst30m.isPresent()) {
                    StrategyPerformance worst30mPerf = worst30m.get();
                    summary.setM30WorstPeriod(worst30mPerf.getPeriodValue());
                    summary.setM30WorstInterval(worst30mPerf.getInterval());
                    summary.setM30WorstReturn(worst30mPerf.getTotalReturn());
                }

                summary.setM30AverageReturn(avgReturn30m);
                summary.setM30AveragePositionCount(avgPositionCount30m);



                Optional<StrategyPerformance> best15m = getMaxReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                Optional<StrategyPerformance> worst15m = getMinReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                double avgReturn15m = getAverageReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                double avgPositionCount15m = getAveragePositionCount(performances, CandlestickInterval.FIFTEEN_MINUTES);

                if (best15m.isPresent()) {
                    StrategyPerformance best15mPerf = best15m.get();
                    summary.setM15BestPeriod(best15mPerf.getPeriodValue());
                    summary.setM15BestInterval(best15mPerf.getInterval());
                    summary.setM15BestReturn(best15mPerf.getTotalReturn());
                }

                if (worst15m.isPresent()) {
                    StrategyPerformance worst15mPerf = worst15m.get();
                    summary.setM15WorstPeriod(worst15mPerf.getPeriodValue());
                    summary.setM15WorstInterval(worst15mPerf.getInterval());
                    summary.setM15WorstReturn(worst15mPerf.getTotalReturn());
                }

                summary.setM15AverageReturn(avgReturn15m);
                summary.setM15AveragePositionCount(avgPositionCount15m);



                Optional<StrategyPerformance> best5m = getMaxReturn(performances, CandlestickInterval.FIVE_MINUTES);
                Optional<StrategyPerformance> worst5m = getMinReturn(performances, CandlestickInterval.FIVE_MINUTES);
                double avgReturn5m = getAverageReturn(performances, CandlestickInterval.FIVE_MINUTES);
                double avgPositionCount5m = getAveragePositionCount(performances, CandlestickInterval.FIVE_MINUTES);

                if (best5m.isPresent()) {
                    StrategyPerformance best5mPerf = best5m.get();
                    summary.setM5BestPeriod(best5mPerf.getPeriodValue());
                    summary.setM5BestInterval(best5mPerf.getInterval());
                    summary.setM5BestReturn(best5mPerf.getTotalReturn());
                }

                if (worst5m.isPresent()) {
                    StrategyPerformance worst5mPerf = worst5m.get();
                    summary.setM5WorstPeriod(worst5mPerf.getPeriodValue());
                    summary.setM5WorstInterval(worst5mPerf.getInterval());
                    summary.setM5WorstReturn(worst5mPerf.getTotalReturn());
                }

                summary.setM5AverageReturn(avgReturn5m);
                summary.setM5AveragePositionCount(avgPositionCount5m);


                if (summary.get_rev() != null) {
                    dbService.updateSummaryByPair(summary);
                } else {
                    dbService.saveSummaryByPair(summary);
                }
            }
        }
    }

    public void prepareSummaryStockByPair(boolean update) {

        List<SimpleStrategy> strategies = StrategyFactory.getAllStrategies();


        for (SimpleStrategy strategy : strategies) {
/*            if (List.of("Cmf", "AroonCmf", "AroonComboShort").contains(strategy.getStrategyName())) {
                continue;
            }*/
            for (String symbol : BackTestConstants.STOCK_SYMBOLS) {
                PerformanceSummaryByPairFx summary = new PerformanceSummaryByPairFx();
                summary.setStrategyName(strategy.getStrategyName());
                summary.setSymbol(symbol);
                summary.set_id(String.join("_", strategy.getStrategyName(), symbol));

                PerformanceSummaryByPairFx existing = dbService.getPerformanceSummaryByPairStock(strategy.getStrategyName(), symbol);
                if (existing != null) {
                    if (update) {
                        summary = existing;
                    } else {
                        continue;
                    }
                }

                List<StrategyPerformance> performances = dbService.getPerformanceByStrategyAndPair("aaa_strategy_performance_stock", strategy.getStrategyName(), symbol);

                Optional<StrategyPerformance> best = getMaxReturn(performances, null);
                Optional<StrategyPerformance> worst = getMinReturn(performances, null);
                double avgReturn = getAverageReturn(performances, null);
                double avgPositionCount = getAveragePositionCount(performances, null);

                if (best.isPresent()) {
                    StrategyPerformance bestPerf = best.get();
                    summary.setBestPeriod(bestPerf.getPeriodValue());
                    summary.setBestInterval(bestPerf.getInterval());
                    summary.setBestReturn(bestPerf.getTotalReturn());
                }

                if (worst.isPresent()) {
                    StrategyPerformance worstPerf = worst.get();
                    summary.setWorstPeriod(worstPerf.getPeriodValue());
                    summary.setWorstInterval(worstPerf.getInterval());
                    summary.setWorstReturn(worstPerf.getTotalReturn());
                }

                summary.setAverageReturn(avgReturn);
                summary.setAveragePositionCount(avgPositionCount);


                Optional<StrategyPerformance> best15m = getMaxReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                Optional<StrategyPerformance> worst15m = getMinReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                double avgReturn15m = getAverageReturn(performances, CandlestickInterval.FIFTEEN_MINUTES);
                double avgPositionCount15m = getAveragePositionCount(performances, CandlestickInterval.FIFTEEN_MINUTES);

                if (best15m.isPresent()) {
                    StrategyPerformance best15mPerf = best15m.get();
                    summary.setM15BestPeriod(best15mPerf.getPeriodValue());
                    summary.setM15BestInterval(best15mPerf.getInterval());
                    summary.setM15BestReturn(best15mPerf.getTotalReturn());
                }

                if (worst15m.isPresent()) {
                    StrategyPerformance worst15mPerf = worst15m.get();
                    summary.setM15WorstPeriod(worst15mPerf.getPeriodValue());
                    summary.setM15WorstInterval(worst15mPerf.getInterval());
                    summary.setM15WorstReturn(worst15mPerf.getTotalReturn());
                }

                summary.setM15AverageReturn(avgReturn15m);
                summary.setM15AveragePositionCount(avgPositionCount15m);


                Optional<StrategyPerformance> best5m = getMaxReturn(performances, CandlestickInterval.FIVE_MINUTES);
                Optional<StrategyPerformance> worst5m = getMinReturn(performances, CandlestickInterval.FIVE_MINUTES);
                double avgReturn5m = getAverageReturn(performances, CandlestickInterval.FIVE_MINUTES);
                double avgPositionCount5m = getAveragePositionCount(performances, CandlestickInterval.FIVE_MINUTES);

                if (best5m.isPresent()) {
                    StrategyPerformance best5mPerf = best5m.get();
                    summary.setM5BestPeriod(best5mPerf.getPeriodValue());
                    summary.setM5BestInterval(best5mPerf.getInterval());
                    summary.setM5BestReturn(best5mPerf.getTotalReturn());
                }

                if (worst5m.isPresent()) {
                    StrategyPerformance worst5mPerf = worst5m.get();
                    summary.setM5WorstPeriod(worst5mPerf.getPeriodValue());
                    summary.setM5WorstInterval(worst5mPerf.getInterval());
                    summary.setM5WorstReturn(worst5mPerf.getTotalReturn());
                }

                summary.setM5AverageReturn(avgReturn5m);
                summary.setM5AveragePositionCount(avgPositionCount5m);


                if (summary.get_rev() != null) {
                    dbService.updateSummaryByPairStock(summary);
                } else {
                    dbService.saveSummaryByPairStock(summary);
                }
            }
        }
    }
}
