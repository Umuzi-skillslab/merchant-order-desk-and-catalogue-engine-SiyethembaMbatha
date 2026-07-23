package com.paynestsystem.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void calculateTotal_multipliesUnitPriceByQuantity() {
        Product mouse = new Product(2, "Wireless Mouse", 350.00);
        OrderItem item = new OrderItem(mouse, 3);

        assertEquals(1050.00, item.calculateTotal(), 0.0001);
    }

    @Test
    void calculateTotal_singleUnit_equalsUnitPrice() {
        Product laptop = new Product(1, "Laptop", 12000.00);
        OrderItem item = new OrderItem(laptop, 1);

        assertEquals(12000.00, item.calculateTotal(), 0.0001);
    }

    @Test
    void constructor_rejectsZeroOrNegativeQuantity() {
        Product mouse = new Product(2, "Wireless Mouse", 350.00);

        assertThrows(IllegalArgumentException.class, () -> new OrderItem(mouse, 0));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(mouse, -1));
    }

    @Test
    void constructor_rejectsNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, 1));
    }
}
