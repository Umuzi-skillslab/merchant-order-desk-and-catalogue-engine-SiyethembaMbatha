package com.paynestsystem.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.paynestsystem.payment.PaymentMethod;
import com.paynestsystem.payment.PaymentProcessor;
import com.paynestsystem.util.Money;

/**
 * An order and its lifecycle. Rules that are always true live here:
 * an order starts unpaid, can only be paid once, and cannot change after
 * it has been paid. The only way to pay is {@link #checkout}, so an order
 * can never be marked paid without the total having been charged.
 */
public class Order {

    // PaymentProcessor holds no state, so one shared instance is safe.
    private static final PaymentProcessor DEFAULT_PAYMENT_PROCESSOR = new PaymentProcessor();

    private final int id;
    private final Customer customer;
    private final List<OrderItem> items;
    private boolean paid;

    public Order(int id, Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Order requires a customer");
        }
        this.id = id;
        this.customer = customer;
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isPaid() {
        return paid;
    }

    public void addItem(Product product, int quantity) {
        if (paid) {
            throw new IllegalStateException("Cannot change order #" + id + ": it is already paid");
        }
        items.add(new OrderItem(product, quantity));
    }

    /** The order total in Rand, exact, always with two decimal places. */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (OrderItem item : items) {
            total = total.add(item.calculateTotal());
        }
        return total;
    }

    public String printSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(id)
                .append(" for ").append(customer.getName())
                .append(" (").append(customer.getEmail()).append(")")
                .append(System.lineSeparator());

        if (items.isEmpty()) {
            sb.append("  (no items)").append(System.lineSeparator());
        } else {
            for (OrderItem item : items) {
                sb.append(String.format("  %-20s x%-3d %10s%n",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        Money.formatRand(item.calculateTotal())));
            }
        }

        sb.append("  ").append("-".repeat(36)).append(System.lineSeparator());
        sb.append(String.format("  %-24s %10s%n", "Grand Total", Money.formatRand(calculateTotal())));
        return sb.toString();
    }

    /**
     * Checks out using the default {@link PaymentProcessor}. Works with any
     * {@link PaymentMethod}; the order never needs to know which rail it is.
     */
    public boolean checkout(PaymentMethod paymentMethod) {
        return checkout(paymentMethod, DEFAULT_PAYMENT_PROCESSOR);
    }

    /**
     * Checkout with an explicitly supplied processor, so the processor can be
     * swapped (or observed in tests) without changing this class.
     */
    public boolean checkout(PaymentMethod paymentMethod, PaymentProcessor paymentProcessor) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Checkout requires a payment method");
        }
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("Checkout requires a payment processor");
        }

        if (paid) {
            throw new IllegalStateException("Order #" + id + " is already paid");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Order #" + id + " has no items to check out");
        }

        BigDecimal amount = calculateTotal();

        System.out.println(printSummary());

        boolean success = paymentProcessor.processPayment(paymentMethod, amount);

        if (success) {
            paid = true;
            System.out.println("Order #" + id + " complete for " + customer.getName()
                    + " via " + paymentMethod.getPaymentType()
                    + " (" + Money.formatRand(amount) + ")");
        } else {
            System.out.println("Order #" + id + " could not be completed for " + customer.getName()
                    + " via " + paymentMethod.getPaymentType());
        }

        return success;
    }
}
