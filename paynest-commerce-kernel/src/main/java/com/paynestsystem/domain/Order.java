package com.paynestsystem.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final int id;
    private final Customer customer;
    private final List<OrderItem> items;

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

    public void addItem(Product product, int quantity) {
        items.add(new OrderItem(product, quantity));
    }

    public double calculateTotal() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.calculateTotal();
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
                        formatRand(item.calculateTotal())));
            }
        }

        sb.append("  ").append("-".repeat(36)).append(System.lineSeparator());
        sb.append(String.format("  %-24s %10s%n", "Grand Total", formatRand(calculateTotal())));
        return sb.toString();
    }

    private static String formatRand(double amount) {
        return String.format(java.util.Locale.US, "R%,.2f", amount);
    }
}
