package com.paynestsystem.service;

import com.paynestsystem.domain.Customer;
import com.paynestsystem.domain.Order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    private final Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");

    @Test
    void createOrder_assignsIncrementingIds() {
        OrderService service = new OrderService();

        Order first = service.createOrder(customer);
        Order second = service.createOrder(customer);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
    }

    @Test
    void createOrder_returnsEmptyOrderForTheCustomer() {
        Order order = new OrderService().createOrder(customer);

        assertTrue(order.getItems().isEmpty());
        assertEquals(customer, order.getCustomer());
    }

    @Test
    void createOrder_rejectsNullCustomer() {
        assertThrows(IllegalArgumentException.class, () -> new OrderService().createOrder(null));
    }
}
