package com.paynestsystem.domain;

import java.math.BigDecimal;

public class OrderItem {

    private final Product product;
    private final int quantity;

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("OrderItem requires a product");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive integer, was: " + quantity);
        }
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal calculateTotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "OrderItem{product=" + product.getName() + ", quantity=" + quantity
                + ", lineTotal=" + calculateTotal() + '}';
    }
}
