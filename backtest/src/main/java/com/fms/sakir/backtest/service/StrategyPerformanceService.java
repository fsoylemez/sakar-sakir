package com.fms.sakir.backtest.service;

import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.model.PerformanceStats;
import com.fms.sakir.backtest.model.StrategyPerformance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class StrategyPerformanceService {

    @Inject
    CouchDbService dbService;


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
                (p1, p2) -> Double.compare(p1.getTotalReturn()/p1.getPositionCount(), p2.getTotalReturn()/p2.getPositionCount());

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
}
