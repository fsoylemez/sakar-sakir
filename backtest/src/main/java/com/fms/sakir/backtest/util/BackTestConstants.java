package com.fms.sakir.backtest.util;

import com.binance.api.client.domain.market.CandlestickInterval;

public class BackTestConstants {

    public static final CandlestickInterval[] INTERVALS = {CandlestickInterval.FOUR_HOURLY, CandlestickInterval.TWO_HOURLY, CandlestickInterval.HOURLY
            , CandlestickInterval.HALF_HOURLY, CandlestickInterval.FIFTEEN_MINUTES, CandlestickInterval.FIVE_MINUTES};

    public static final String[] TICKERS = {"aaveusdt","solusdt","dotusdt","kavausdt","atomusdt","ethusdt","btcusdt","sushiusdt","avaxusdt","xtzusdt"};

    public static final String[] FX_PAIRS = {"usdcad", "eurjpy", "eurusd", "eurchf", "usdchf", "eurgbp", "gbpusd", "audcad", "nzdusd",
            "gbpchf", "audusd", "gbpjpy", "usdjpy", "chfjpy", "eurcad", "audjpy", "euraud", "audnzd", "xauusd"};

}
