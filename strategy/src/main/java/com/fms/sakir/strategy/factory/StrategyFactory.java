package com.fms.sakir.strategy.factory;

import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.exception.SakirException;
import com.fms.sakir.strategy.strategies.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StrategyFactory {

    private static List<SimpleStrategy> strategies;

    public static List<SimpleStrategy> getStrategies(List<String> strategyNames) {
        if (strategyNames==null || strategyNames.isEmpty())
            return getAllStrategies();
        return getAllStrategies().stream().filter(s -> strategyNames.contains(s.getStrategyName())).collect(Collectors.toList());
    }

    public static List<SimpleStrategy> getAllStrategies() {
/*        if (CollectionUtils.isEmpty(strategies)) {
            loadStrategies();
        }*/
        loadStrategies();

        return strategies;
    }

    private static void loadStrategies() {
/*        if (strategies == null) {
            strategies = new ArrayList<>();
        }*/
        strategies = new ArrayList<>();

        strategies.add(new CMFStrategy("Cmf",20, -.15, .15));
        strategies.add(new MacdStrategy("Macd",12, 26, 9));
        strategies.add(new StochasticRSIStrategy("StochRsi",14, .15, .75));
        strategies.add(new MyStrategy("My",14, .15, .75));
        strategies.add(new AroonStrategy("Aroon",20));
        strategies.add(new AroonShortStrategy("AroonShort",7));
        strategies.add(new AroonCmfStrategy("AroonCmf",7, 18));
        strategies.add(new AroonStochStrategy("AroonStoch",7, 14));
        strategies.add(new AroonComboStrategy("AroonCombo",7,  14));
        strategies.add(new AroonComboShortStrategy("AroonComboShort",7, 18, 14));
        strategies.add(new AroonCombo2Strategy("AroonCombo2",7, 18, 14));
        strategies.add(new AroonCombo3Strategy("AroonCombo3",20, 18, 14));
        strategies.add(new ComboSolEthStrategy("ComboSolEth",20, 14));
        strategies.add(new ComboSolEth1Strategy("ComboSolEth1",20, 14));
        strategies.add(new ComboSolEth2Strategy("ComboSolEth2",20, 14));
        strategies.add(new ComboStrategy("Combo",20, 18, 14));
        strategies.add(new AroonCombo4FStrategy("AroonCombo4F",20, 14));
        strategies.add(new Combo2Strategy("Combo2",20, 18, 14));
        strategies.add(new ComboSolEthCciStrategy("ComboSolEthCci",20, 14, 25));
        strategies.add(new ComboSolEthCci2Strategy("ComboSolEthCci2",20, 14, 25, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("ComboSolEthCci3",20, 14, 20, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("ComboSolEthCci4",20, 14, 20, -100, -100));
        strategies.add(new CciStrategy("Cci",25, -100, 100));
        strategies.add(new Cci7Strategy("Cci7",13));
        strategies.add(new Cci7Strategy("Cci20",20));



    }

    public static Strategy getStrategyByName(String name, BarSeries series) throws SakirException {
         Optional<SimpleStrategy> strategy = getAllStrategies().stream().filter(s->s.getStrategyName().equals(name)).findFirst();

         return strategy.map(s-> s.buildStrategy(series)).orElseThrow(() -> new SakirException("Specified Strategy could not be found"));
    }

    public static SimpleStrategy getStrategyByName(String name) throws SakirException {
        return getAllStrategies().stream().filter(s->s.getStrategyName().equals(name)).findFirst().orElseThrow(() -> new SakirException("Specified Strategy could not be found"));
    }
}
