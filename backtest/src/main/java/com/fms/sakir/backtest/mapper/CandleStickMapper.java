package com.fms.sakir.backtest.mapper;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.util.DateUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZonedDateTime;

@ApplicationScoped
public class CandleStickMapper {

    public Bar toBar(Candlestick candlestick, CandlestickInterval interval) {

        ZonedDateTime closeTime = Instant.ofEpochMilli(candlestick.getCloseTime()).atZone(DateUtils.getZoneId());

        return new BaseBar(DateUtils.intervalToDuration(interval), closeTime, candlestick.getOpen(),
                candlestick.getHigh(), candlestick.getLow(),
                candlestick.getClose(), candlestick.getVolume());
    }
}
