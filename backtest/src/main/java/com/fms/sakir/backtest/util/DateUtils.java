package com.fms.sakir.backtest.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;


    public static Long dateToMilli(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        LocalDate localDate = LocalDate.parse(dateStr, dateFormatter);
        return localDate.atStartOfDay().atZone(getZoneId()).toInstant().toEpochMilli();
    }

    public static ZoneId getZoneId() {
        return ZoneId.of("Europe/Riga");
    }

    public static Duration intervalToDuration(CandlestickInterval interval) {

        Duration convertedDuration;
        switch (interval) {
            case ONE_MINUTE:
                convertedDuration = Duration.ofMinutes(1);
                break;
            case FIVE_MINUTES:
                convertedDuration = Duration.ofMinutes(5);
                break;
            case THREE_MINUTES:
                convertedDuration = Duration.ofMinutes(3);
                break;
            case FIFTEEN_MINUTES:
                convertedDuration = Duration.ofMinutes(15);
                break;
            case HALF_HOURLY:
                convertedDuration = Duration.ofMinutes(30);
                break;
            case HOURLY:
                convertedDuration = Duration.ofHours(1);
                break;
            case FOUR_HOURLY:
                convertedDuration = Duration.ofHours(4);
                break;
            case DAILY:
                convertedDuration = Duration.ofDays(1);
                break;
            case WEEKLY:
                convertedDuration = Duration.ofDays(7);
                break;
            default:
                convertedDuration = Duration.ofMinutes(15);
        }

        return convertedDuration;
    }
}
