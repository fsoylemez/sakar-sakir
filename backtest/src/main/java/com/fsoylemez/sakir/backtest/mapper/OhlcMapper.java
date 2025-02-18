package com.fsoylemez.sakir.backtest.mapper;

import com.fsoylemez.sakir.backtest.enums.TiingoInterval;
import com.fsoylemez.sakir.backtest.model.tiingo.OhlcData;
import com.fsoylemez.sakir.backtest.util.DateUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OhlcMapper {

    public Bar toBar(OhlcData ohlc, TiingoInterval interval) {

        ZonedDateTime closeTime = Instant.ofEpochMilli(ohlc.getDate()).atZone(DateUtils.getZoneId());

        return new BaseBar(DateUtils.intervalToDuration(interval), closeTime, ohlc.getOpen().toString(),
                ohlc.getHigh().toString(), ohlc.getLow().toString(),
                ohlc.getClose().toString(),  "0");
    }

    public List<Bar> toBar(List<OhlcData> data, TiingoInterval interval) {
        return data.stream().map(o -> toBar(o, interval)).collect(Collectors.toList());
    }
}
