package com.paynestsystem.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void calculateTotal_multipliesUnitPriceByQuantity() {
        Product mouse = new Product(2, "Wireless Mouse", new BigDecimal("350.00"));
        OrderItem item = new OrderItem(mouse, 3);

        assertEquals(new BigDecimal("1050.00"), item.calculateTotal());
    }

    @Test
    void calculateTotal_singleUnit_equalsUnitPrice() {
        Product laptop = new Product(1, "Laptop", new BigDecimal("12000.00"));
        OrderItem item = new OrderItem(laptop, 1);

        assertEquals(new BigDecimal("12000.00"), item.calculateTotal());
    }

    @Test
    void constructor_rejectsZeroOrNegativeQuantity() {
        Product mouse = new Product(2, "Wireless Mouse", new BigDecimal("350.00"));

        assertThrows(IllegalArgumentException.class, () -> new OrderItem(mouse, 0));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(mouse, -1));
    }

    @Test
    void constructor_rejectsNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, 1));
    }
}
