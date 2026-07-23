package com.paynestsystem.service;

import com.paynestsystem.domain.Customer;
import com.paynestsystem.domain.Order;

public class OrderService {

    private int nextOrderId = 1;

    /**
     * Creates a new, empty order for the given customer with an
     * auto-assigned id. Callers add items afterwards via
     * {@link Order#addItem(com.paynestsystem.domain.Product, int)}.
     */
    public Order createOrder(Customer customer) {
        return new Order(nextOrderId++, customer);
    }
}
