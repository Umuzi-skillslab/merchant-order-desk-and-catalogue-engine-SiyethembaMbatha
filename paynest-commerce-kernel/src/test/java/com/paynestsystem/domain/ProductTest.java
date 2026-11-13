package com.paynestsystem.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void validProduct_exposesItsFields() {
        Product laptop = new Product(1, "Laptop", new BigDecimal("12000.00"));

        assertEquals(1, laptop.getId());
        assertEquals("Laptop", laptop.getName());
        assertEquals(new BigDecimal("12000.00"), laptop.getPrice());
    }

    @Test
    void freeProduct_isAllowed() {
        assertEquals(new BigDecimal("0.00"), new Product(3, "Sticker", BigDecimal.ZERO).getPrice());
    }

    @Test
    void price_isAlwaysStoredWithTwoDecimalPlaces() {
        assertEquals(new BigDecimal("350.00"), new Product(2, "Mouse", new BigDecimal("350")).getPrice());
        assertEquals(new BigDecimal("9.90"), new Product(2, "Cable", new BigDecimal("9.9")).getPrice());
        assertEquals(new BigDecimal("12.50"), new Product(2, "Cable", new BigDecimal("12.500")).getPrice());
    }

    @Test
    void constructor_rejectsBlankOrNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Product(1, null, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Product(1, "", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Product(1, "   ", BigDecimal.TEN));
    }

    @Test
    void constructor_rejectsNullPrice() {
        assertThrows(IllegalArgumentException.class, () -> new Product(1, "Laptop", null));
    }

    @Test
    void constructor_rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "Laptop", new BigDecimal("-0.01")));
    }

    @Test
    void constructor_rejectsSubCentPricesInsteadOfRoundingSilently() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "Laptop", new BigDecimal("9.999")));
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "Laptop", new BigDecimal("0.001")));
    }
}
