package com.fms.sakir.backtest.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.enums.TiingoInterval;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

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

    public static ZoneId getUtcZoneId() {
        return ZoneId.of("UTC");
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

    public static Duration intervalToDuration(TiingoInterval interval) {

        Duration convertedDuration;
        switch (interval) {
            case M1:
                convertedDuration = Duration.ofMinutes(1);
                break;
            case M5:
                convertedDuration = Duration.ofMinutes(5);
                break;
            case M15:
                convertedDuration = Duration.ofMinutes(15);
                break;
            case H1:
                convertedDuration = Duration.ofHours(1);
                break;
            case H4:
                convertedDuration = Duration.ofHours(4);
                break;
            case D1:
                convertedDuration = Duration.ofDays(1);
                break;
            default:
                convertedDuration = Duration.ofMinutes(15);
        }

        return convertedDuration;
    }

    public static long getDueDate(CandlestickInterval candlestickInterval) {
        ZonedDateTime today = ZonedDateTime.now(DateUtils.getZoneId());

        if (candlestickInterval.equals(CandlestickInterval.FIFTEEN_MINUTES)) {
            int minute = today.get(ChronoField.MINUTE_OF_HOUR);
            int diff = minute % 15;
            return today.minus(diff, ChronoUnit.MINUTES).toInstant().toEpochMilli();
        }
        if (candlestickInterval.equals(CandlestickInterval.FIVE_MINUTES)) {
            int minute = today.get(ChronoField.MINUTE_OF_HOUR);
            int diff = minute % 5;
            return today.minus(diff, ChronoUnit.MINUTES).toInstant().toEpochMilli();
        }
        if (candlestickInterval.equals(CandlestickInterval.HOURLY)) {
            int minute = today.get(ChronoField.MINUTE_OF_HOUR);
            return today.minus(minute, ChronoUnit.MINUTES).toInstant().toEpochMilli();
        }
        if (candlestickInterval.equals(CandlestickInterval.FOUR_HOURLY)) {
            int minute = today.get(ChronoField.MINUTE_OF_HOUR);
            int hour = today.get(ChronoField.HOUR_OF_DAY);
            int diff = hour % 4;
            today = today.minus(diff, ChronoUnit.HOURS);
            return today.minus(minute, ChronoUnit.MINUTES).toInstant().toEpochMilli();
        }
        if (candlestickInterval.equals(CandlestickInterval.HALF_HOURLY)) {
            int minute = today.get(ChronoField.MINUTE_OF_HOUR);
            int diff = minute % 30;
            return today.minus(diff, ChronoUnit.MINUTES).toInstant().toEpochMilli();
        }
        if (candlestickInterval.equals(CandlestickInterval.TWO_HOURLY)) {
            int minute = today.get(ChronoField.MINUTE_OF_HOUR);
            int hour = today.get(ChronoField.HOUR_OF_DAY);
            int diff = hour % 2;
            today = today.minus(diff, ChronoUnit.HOURS);
            return today.minus(minute, ChronoUnit.MINUTES).toInstant().toEpochMilli();
        }

        return today.truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli();
    }
}
