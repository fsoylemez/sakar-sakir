package com.fms.sakir.strategy.factory;

import com.fms.sakir.strategy.base.SimpleStrategy;
import com.fms.sakir.strategy.exception.SakirException;
import com.fms.sakir.strategy.strategies.*;
import com.fms.sakir.strategy.strategies.v2.*;
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
        strategies.add(new AroonExitStrategy("My",7));
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
        strategies.add(new Combo2Strategy("Combo2Old",20, 18, 14));
        strategies.add(new ComboSolEthCciStrategy("ComboSolEthCci",20, 14, 25));
        strategies.add(new ComboSolEthCci2Strategy("ComboSolEthCci2",20, 14, 25, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("ComboSolEthCci3",20, 14, 20, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("ComboSolEthCci4",20, 14, 20, -100, -100));
        strategies.add(new CciStrategy("Cci",25, -100, 100));
        strategies.add(new Cci7Strategy("Cci7",13));
        strategies.add(new Cci7Strategy("Cci20",20));
        /*************new strategies*****************/
        strategies.add(new StochCciSarStrategy("StochCciSar",14,25, 30, 70));
        strategies.add(new AroonSar3Strategy("AroonSar3",20));
        strategies.add(new AroonSar2Strategy("AroonSar2",20));
        strategies.add(new SarStrategy("Sar"));
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
        strategies.add(new ComboSolEthCci2Strategy("Combo2New",20, 14, 20, -100, -100));
        strategies.add(new AroonComboStrategy("Aroon13Combo",13,  14));

        /*************Aroon 7 ********/
        strategies.add(new AroonStrategy("7Aroon",7));
        strategies.add(new ComboSolEthStrategy("7ComboSolEth",7, 14));
        strategies.add(new ComboSolEth1Strategy("7ComboSolEth1",7, 14));
        strategies.add(new ComboSolEth2Strategy("7ComboSolEth2",7, 14));
        strategies.add(new ComboStrategy("7Combo",7, 18, 14));
        strategies.add(new AroonCombo4FStrategy("7AroonCombo4F",7, 14));
        strategies.add(new Combo2Strategy("7Combo2Old",7, 18, 14));
        strategies.add(new ComboSolEthCciStrategy("7ComboSolEthCci",7, 14, 25));
        strategies.add(new ComboSolEthCci2Strategy("7ComboSolEthCci2",7, 14, 25, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("7ComboSolEthCci3",7, 14, 20, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("7ComboSolEthCci4",7, 14, 20, -100, -100));
        strategies.add(new AroonSar3Strategy("7AroonSar3",7));
        strategies.add(new AroonSar2Strategy("7AroonSar2",7));
        strategies.add(new AroonCciStrategy("7AroonCci",7, 20, -100));
        strategies.add(new AroonCciStrategy("7AroonCci0",7, 20, 0));
        strategies.add(new AroonSarStrategy("7AroonSar",7));
        strategies.add(new AroonStochV2Strategy("7AroonStochV2",7, 14));
        strategies.add(new AroonStochV3Strategy("7AroonStochV3",7, 14));
        strategies.add(new AroonStochV4Strategy("7AroonStochV4",7, 14));
        strategies.add(new ComboSolEthCci2Strategy("7Combo1",7, 14, 20, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("7Combo1Cci25",7, 14, 25, -100, 100));
        strategies.add(new ComboSolEthCci2Strategy("7Combo2New",7, 14, 20, -100, -100));
        strategies.add(new AroonComboStrategy("AroonCombo7Stoch",7,  7));
        strategies.add(new AroonFisherStrategy("AroonFisher",7,  14));
        strategies.add(new AroonFisher2Strategy("AroonFisher2",7,  14));
        strategies.add(new AroonComboCciStrategy("AroonComboCci",7,  14, 20));
        strategies.add(new StochSar2Strategy("StochSar2",14, 30, 70, 0d));
        strategies.add(new StochSar2Strategy("StochSar3",14, 30, 70, 1d));
        strategies.add(new SarCciStrategy("SarCci", 7, 100, -100));
        strategies.add(new SarCci2Strategy("SarCci2", 7, -100, 1d));
        strategies.add(new SarCci2Strategy("SarCci3", 7, -100, 0.7));
        strategies.add(new StochSar2Strategy("StochSar4",14, 30, 70, 0.7));
        strategies.add(new SarCciFisherStrategy("SarCciFisher",7, -100, 0.7));
        strategies.add(new SarCciDStrategy("SarCciD", 7, -100, 0.7));
        strategies.add(new StochSarSimpleStrategy("StochSarSimple",14, 30));
        strategies.add(new EngulfingStrategy("Engulfing"));
        strategies.add(new EngulfingEmaStrategy("EngulfingEma"));
        strategies.add(new EngulfingEmaFisherStrategy("EngulfingEmaFisher", 200));
        strategies.add(new EngulfingEmaFisherStrategy("EngulfingEmaFisher50", 50));
        strategies.add(new EngulfingCciStrategy("EngulfingCci", 20));


    }

    public static Strategy getStrategyByName(String name, BarSeries series) throws SakirException {
         Optional<SimpleStrategy> strategy = getAllStrategies().stream().filter(s->s.getStrategyName().equals(name)).findFirst();

         return strategy.map(s-> s.buildStrategy(series)).orElseThrow(() -> new SakirException("Specified Strategy could not be found"));
    }

    public static SimpleStrategy getStrategyByName(String name) throws SakirException {
        return getAllStrategies().stream().filter(s->s.getStrategyName().equals(name)).findFirst().orElseThrow(() -> new SakirException("Specified Strategy could not be found"));
    }
}
