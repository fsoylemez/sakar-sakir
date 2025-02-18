package com.fsoylemez.sakir.backtest.util;

import com.binance.api.client.domain.market.CandlestickInterval;

import java.util.Arrays;

public class StringUtils {

    public static String buildDbName(String ticker, CandlestickInterval interval) {
        return String.join("_", ticker, interval.getIntervalId());
    }

    public static String buildStrategyPerfId(String... values) {
        return Arrays.stream(values).map(s-> s.replace(" ","")).reduce(String::concat).orElse("");
    }
}
