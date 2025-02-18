package com.fsoylemez.sakar.sakir.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateUtilsTest {

    @Test
    void testTz() {
        Instant instant = Instant.now();
        System.out.println(instant);
        ZonedDateTime zdt = instant.atZone(DateUtils.getZoneId());
        System.out.println(zdt);
        assertNotNull(zdt);
    }
}
