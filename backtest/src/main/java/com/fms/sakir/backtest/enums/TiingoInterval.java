package com.fms.sakir.backtest.enums;

import com.binance.api.client.domain.market.CandlestickInterval;

public enum TiingoInterval {
    D1("1day"),
    H4("4hour"),
    H1("1hour"),
    M15("15min"),
    M5("5min"),
    M1("1min");

    private String value;

    TiingoInterval(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TiingoInterval getByValue(String resampleFreq) {
        for (TiingoInterval c : values()) {
            if (c.value.equals(resampleFreq)) {
                return c;
            }
        }

        throw new IllegalArgumentException(resampleFreq);
    }

    public static TiingoInterval getByCandlestickInterval(CandlestickInterval candlestickInterval) {
        switch (candlestickInterval) {
            case ONE_MINUTE:
                return M1;
            case FIVE_MINUTES:
                return M5;
            case FIFTEEN_MINUTES:
                return M15;
            case HOURLY:
                return H1;
            case FOUR_HOURLY:
                return H4;
            default:
                return D1;
        }
    }
}
