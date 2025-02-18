package com.fsoylemez.sakar.sakir.service.binance.v2;

import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.exception.BinanceApiException;
import com.fsoylemez.sakar.sakir.messaging.TelegramMessenger;
import com.fsoylemez.sakar.sakir.service.binance.BinanceAccountService;
import com.fsoylemez.sakar.sakir.service.binance.BinanceSpotTradeService;
import com.fsoylemez.sakir.strategy.base.SimpleStrategy;
import com.fsoylemez.sakir.strategy.exception.SakirException;
import com.fsoylemez.sakir.strategy.factory.StrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static com.fsoylemez.sakar.sakir.util.NumberUtils.convertPrecision;

@Slf4j
@ApplicationScoped
public class BinancePositionServiceV2 {

    private static final String BASE_SYMBOL = "USDT";

    private final Map<String, TradingRecord> tradingRecords = new HashMap<>();

    @Inject
    BinanceAccountService accountService;

    @Inject
    BinanceSpotTradeService tradeService;

    @Inject
    TelegramMessenger messenger;

    public void evaluateEntryOrExit(Deque<Bar> bars, String symbol, String strategyName) throws SakirException {
        Bar lastBar = bars.peekLast();

        SimpleStrategy simpleStrategy = StrategyFactory.getStrategyByName(strategyName);

        if (tradingRecords.get(symbol) == null) {
            TradingRecord tradingRecord = new BaseTradingRecord(simpleStrategy.getTradeType());
            Trade lastTrade = accountService.getLastTrade(symbol);
            if (lastTrade.isBuyer() && accountService.getFreeBalance(symbol) != null) {
                tradingRecord.enter(0, DecimalNum.valueOf(lastTrade.getPrice()), DecimalNum.valueOf(lastTrade.getQty()));
            }

            tradingRecords.put(symbol, tradingRecord);
        }

        TradingRecord tradingRecord = tradingRecords.get(symbol);

        BarSeries barSeries = new BaseBarSeries(String.join("_", symbol, strategyName));
        bars.forEach(barSeries::addBar);

        Strategy strategy = simpleStrategy.buildStrategy(barSeries);

        try {
            int endIndex = barSeries.getEndIndex();
            if (tradingRecord.isClosed()) {
                if (strategy.shouldEnter(endIndex, tradingRecord)) {
                    enterPosition(lastBar, endIndex, symbol, tradingRecord);
                }
            } else {
                if (strategy.shouldExit(endIndex, tradingRecord)) {
                    exitPosition(lastBar, endIndex, symbol, tradingRecord);
                }
            }

        } catch (BinanceApiException e) {
            log.error("Binance exception occured.Message: {}", e.getMessage());
        }
    }

    private void enterPosition(Bar bar, int index, String symbol, TradingRecord tradingRecord) {
        String usdtBalance = accountService.getFreeBalance(BASE_SYMBOL);
        if (usdtBalance != null) {
            double availableUsdt = Double.parseDouble(usdtBalance);
            if (availableUsdt > 0) {
                log.info("#####opening position#####");
                DecimalNum quantity = DecimalNum.valueOf(availableUsdt / bar.getClosePrice().doubleValue());

                tradeService.marketBuyOrder(symbol, quantity.toString());
                tradingRecord.enter(index, bar.getClosePrice(), quantity);

                Position currentPosition = tradingRecord.getCurrentPosition();
                log.info("Opened position entry price: {}", currentPosition.getEntry().getPricePerAsset());

                messenger.sendToTelegram(String.format("Opened Position Candle Time: %s Price: %s", bar.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm")),
                        convertPrecision(bar.getClosePrice().toString(), 4)));

            } else {
                log.error("Can't open position, zero {} balance.", BASE_SYMBOL);
            }
        } else {
            log.error("Can't open position, no {} balance.", BASE_SYMBOL);
        }
    }

    private void exitPosition(Bar bar, int index, String symbol, TradingRecord tradingRecord) {
        String coinSymbol = symbol.replace(BASE_SYMBOL, "");
        String coinBalance = accountService.getFreeBalance(coinSymbol);
        if (coinBalance != null) {
            double availableCoin = Double.parseDouble(coinBalance);
            if (availableCoin > 0) {
                log.info("#####closing position#####");

                tradeService.marketSellOrder(symbol, String.valueOf(availableCoin));
                tradingRecord.exit(index, bar.getClosePrice(), DecimalNum.valueOf(availableCoin));

                Position lastPosition = tradingRecord.getLastPosition();
                String grossReturn = convertPrecision(lastPosition.getGrossReturn().toString(), 4);

                log.info("Closed position entry price: {} exit price :{} return : {}", lastPosition.getEntry().getPricePerAsset(),
                        lastPosition.getExit().getPricePerAsset(), grossReturn);

                messenger.sendToTelegram(String.format("Closed Position Candle Time: %s Entry Price: %s Exit Price : %s return : %s",
                        bar.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm")), convertPrecision(lastPosition.getEntry().getPricePerAsset().toString(), 4),
                        convertPrecision(lastPosition.getExit().getPricePerAsset().toString(), 4), grossReturn));
            } else {
                log.error("Can't close position, zero {} balance.", coinSymbol);
            }
        } else {
            log.error("Can't close position, no {} balance.", coinSymbol);
        }
    }
}
