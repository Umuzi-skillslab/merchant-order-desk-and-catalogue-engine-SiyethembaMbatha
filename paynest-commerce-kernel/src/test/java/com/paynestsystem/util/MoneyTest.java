package com.paynestsystem.util;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void formatRand_usesThousandsSeparatorAndTwoDecimals() {
        assertEquals("R13,050.00", Money.formatRand(new BigDecimal("13050.00")));
    }

    @Test
    void formatRand_padsToTwoDecimals() {
        assertEquals("R1,234.50", Money.formatRand(new BigDecimal("1234.5")));
        assertEquals("R0.00", Money.formatRand(BigDecimal.ZERO));
        assertEquals("R350.00", Money.formatRand(new BigDecimal("350")));
    }

    @Test
    void formatRand_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> Money.formatRand(null));
    }
}
