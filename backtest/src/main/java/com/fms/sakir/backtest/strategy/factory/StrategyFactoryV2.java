package com.fms.sakir.backtest.strategy.factory;

import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.exception.SakirException;
import com.fms.sakir.strategy.strategies.AroonStrategy;
import com.fms.sakir.strategy.strategies.CciStrategy;
import com.fms.sakir.strategy.strategies.ComboSolEthCci2Strategy;
import com.fms.sakir.strategy.strategies.StochConjuctionStrategy;
import com.fms.sakir.strategy.strategies.v2.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StrategyFactoryV2 {

    public static List<SimpleStrategy> getAll() {
        List<SimpleStrategy> strategies = new ArrayList<>();
        strategies.add(new StochCciSar2Strategy("StochCciSar2",14,25, 30));
        strategies.add(new StochCciSarStrategy("StochCciSar",14,25, 30, 70));
        strategies.add(new AroonSar3Strategy("AroonSar3",20));
        strategies.add(new AroonSar2Strategy("AroonSar2",20));
        strategies.add(new SarStrategy("Sar"));
        strategies.add(new SarExStrategy("SarEx"));
        strategies.add(new StochCciStrategy("StochCci", 14,25, 30, 70));
        strategies.add(new StochConjuctionStrategy("StochConj14", 14,30, 70));
        strategies.add(new CciStrategy("Cci25",25, -100, 100));
        strategies.add(new CciMAStrategy("CciMA",25));
        strategies.add(new CciEMAStrategy("CciEMA",25));
        strategies.add(new CciFisherStrategy("CciFisher",25, -100, 100));
        strategies.add(new AroonCciStrategy("AroonCci",25, 20, -100));
        strategies.add(new AroonCciStrategy("AroonCci0",25, 20, 0));
        strategies.add(new AroonStrategy("Aroon20",20));
        strategies.add(new AroonSarStrategy("AroonSar",20));
        strategies.add(new StochSarStrategy("StochSar",14, 30, 70));
        strategies.add(new AroonStochV2Strategy("AroonStochV2",20, 14));
        strategies.add(new AroonStochV3Strategy("AroonStochV3",20, 14));
        strategies.add(new AroonStochV4Strategy("AroonStochV4",20, 14));
        strategies.add(new StochFisherStrategy("StochFisher",20, 30, 70));
        strategies.add(new StochFisherV2Strategy("StochFisher2",20, 30, 70));
        strategies.add(new ComboSolEthCci2Strategy("Combo1",20, 14, 20, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("Combo1Cci25",20, 14, 25, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("Combo2",20, 14, 20, -100, -100));



        return strategies;
    }

    public static List<SimpleStrategy> getAll(List<String> strategyNames) {
        if (strategyNames==null || strategyNames.isEmpty())
            return getAll();
        return getAll().stream().filter(s -> strategyNames.contains(s.getStrategyName())).collect(Collectors.toList());
    }

    public static SimpleStrategy getStrategyByName(String name) throws SakirException {
        return getAll().stream().filter(s->s.getStrategyName().equals(name)).findFirst().orElseThrow(() -> new SakirException("Specified Strategy could not be found"));
    }
}
