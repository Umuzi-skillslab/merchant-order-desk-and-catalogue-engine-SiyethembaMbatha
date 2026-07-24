package com.paynestsystem.app;

import com.paynestsystem.domain.Customer;
import com.paynestsystem.domain.Order;
import com.paynestsystem.domain.Product;
import com.paynestsystem.service.OrderService;

/**
 * Manual end-to-end demo: create products and a customer, build an order
 * through OrderService, add line items, and print the reconcilable summary.
 *
 * Run with: mvn exec:java
 */
public class PayNestApplication {

    public static void main(String[] args) {
        // Sample catalogue
        Product laptop = new Product(1, "Laptop", 12000.00);
        Product mouse = new Product(2, "Wireless Mouse", 350.00);

        // Sample customer
        Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");

        // Build the order through the service layer
        OrderService orderService = new OrderService();
        Order order = orderService.createOrder(customer);

        // Add line items — one single unit, one with quantity > 1
        order.addItem(laptop, 1);
        order.addItem(mouse, 3);

        System.out.println(order.printSummary());
    }
}
