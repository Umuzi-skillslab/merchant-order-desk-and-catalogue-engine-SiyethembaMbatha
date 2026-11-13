package com.paynestsystem.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    private final Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");
    private final Product laptop = new Product(1, "Laptop", new BigDecimal("12000.00"));
    private final Product mouse = new Product(2, "Wireless Mouse", new BigDecimal("350.00"));

    @Test
    void emptyOrder_hasZeroTotal() {
        Order order = new Order(1, customer);

        assertEquals(new BigDecimal("0.00"), order.calculateTotal());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void singleLine_totalEqualsLineSubtotal() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        assertEquals(new BigDecimal("12000.00"), order.calculateTotal());
    }

    @Test
    void multipleLines_totalEqualsSumOfSubtotals() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);
        order.addItem(mouse, 3);

        assertEquals(new BigDecimal("13050.00"), order.calculateTotal());
    }

    @Test
    void addItem_rejectsNonPositiveQuantity() {
        Order order = new Order(1, customer);

        assertThrows(IllegalArgumentException.class, () -> order.addItem(laptop, 0));
        assertThrows(IllegalArgumentException.class, () -> order.addItem(laptop, -5));

        assertEquals(new BigDecimal("0.00"), order.calculateTotal());
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

    @Test
    void total_isExactAndHasTwoDecimalPlaces() {
        Order order = new Order(1, customer);
        order.addItem(new Product(7, "Sweet", new BigDecimal("0.10")), 1);
        order.addItem(new Product(8, "Gum", new BigDecimal("0.20")), 1);

        // 0.1 + 0.2 is 0.30000000000000004 in double arithmetic
        assertEquals(new BigDecimal("0.30"), order.calculateTotal());
    }

    @Test
    void emptyOrderTotal_isZeroWithTwoDecimalPlaces() {
        assertEquals(new BigDecimal("0.00"), new Order(1, customer).calculateTotal());
    }
}
