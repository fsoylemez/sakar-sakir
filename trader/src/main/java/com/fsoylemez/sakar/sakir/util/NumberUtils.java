package com.fsoylemez.sakar.sakir.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

    public static String convertPrecision(String number, int scale) {
        BigDecimal value = new BigDecimal(number);
        BigDecimal converted = value.setScale(scale, RoundingMode.DOWN);

        return converted.toPlainString();
    }
}
