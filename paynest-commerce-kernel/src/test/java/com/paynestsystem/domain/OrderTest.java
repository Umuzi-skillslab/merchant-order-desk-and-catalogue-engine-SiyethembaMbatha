package com.paynestsystem.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    private final Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");
    private final Product laptop = new Product(1, "Laptop", 12000.00);
    private final Product mouse = new Product(2, "Wireless Mouse", 350.00);

    @Test
    void emptyOrder_hasZeroTotal() {
        Order order = new Order(1, customer);

        assertEquals(0.0, order.calculateTotal(), 0.0001);
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void singleLine_totalEqualsLineSubtotal() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        assertEquals(12000.00, order.calculateTotal(), 0.0001);
    }

    @Test
    void multipleLines_totalEqualsSumOfSubtotals() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);
        order.addItem(mouse, 3);

        assertEquals(13050.00, order.calculateTotal(), 0.0001);
    }

    @Test
    void addItem_rejectsNonPositiveQuantity() {
        Order order = new Order(1, customer);

        assertThrows(IllegalArgumentException.class, () -> order.addItem(laptop, 0));
        assertThrows(IllegalArgumentException.class, () -> order.addItem(laptop, -5));
        
        assertEquals(0.0, order.calculateTotal(), 0.0001);
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void getItems_returnsUnmodifiableView() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        assertThrows(UnsupportedOperationException.class,
                () -> order.getItems().add(new OrderItem(mouse, 1)));
    }

    @Test
    void printSummary_includesCustomerEachLineAndGrandTotal() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);
        order.addItem(mouse, 3);

        String summary = order.printSummary();

        assertTrue(summary.contains("Thandiwe Nkosi"));
        assertTrue(summary.contains("Laptop"));
        assertTrue(summary.contains("Wireless Mouse"));
        assertTrue(summary.contains("Grand Total"));
        assertTrue(summary.contains("R13,050.00"));
    }
}
