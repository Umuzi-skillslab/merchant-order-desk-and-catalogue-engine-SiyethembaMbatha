package com.paynestsystem.util;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Single place that decides how a rand amount is shown on receipts and in
 * console output, so every line prints the same value the same way
 * (e.g. {@code R13,050.00}).
 */
public final class Money {

    private Money() {
    }

    public static String formatRand(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        return String.format(Locale.US, "R%,.2f", amount);
    }
}
