package com.fsoylemez.sakir.backtest.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateToLongDeserializer extends JsonDeserializer<Long> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public Long deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException {
        String dateAsString = jsonparser.getText();
        Instant instant = ZonedDateTime.parse(dateAsString, formatter).toInstant();
        return instant.toEpochMilli();
    }
}