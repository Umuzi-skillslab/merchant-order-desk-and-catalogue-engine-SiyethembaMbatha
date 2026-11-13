package com.paynestsystem.app;

import java.math.BigDecimal;

import com.paynestsystem.domain.Customer;
import com.paynestsystem.domain.Order;
import com.paynestsystem.domain.Product;
import com.paynestsystem.payment.CardPayment;
import com.paynestsystem.payment.PaymentMethod;
import com.paynestsystem.service.OrderService;

/**
 * Composition root and manual end-to-end demo: the only class that chooses a
 * concrete payment rail. It builds the Capstone 1 order through OrderService,
 * then checks it out through the {@link PaymentMethod} interface type.
 *
 * <p>To demo a different rail, change only the construction line
 * ({@code new EftPayment()} or {@code new WalletPayment()}).
 *
 * Run with: mvn compile exec:java
 */
public class PayNestApplication {

    public static void main(String[] args) {
        // Sample catalogue
        Product laptop = new Product(1, "Laptop", new BigDecimal("12000.00"));
        Product mouse = new Product(2, "Wireless Mouse", new BigDecimal("350.00"));

        // Sample customer
        Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");

        // Build the order through the service layer
        OrderService orderService = new OrderService();
        Order order = orderService.createOrder(customer);

        // Add line items — one single unit, one with quantity > 1
        order.addItem(laptop, 1);
        order.addItem(mouse, 3);

        // The only line that names a concrete rail. Everything after it sees the interface.
        PaymentMethod paymentMethod = new CardPayment();
        order.checkout(paymentMethod);
    }
}
