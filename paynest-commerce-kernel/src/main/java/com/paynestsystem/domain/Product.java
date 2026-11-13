package com.paynestsystem.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product {

    private final int id;
    private final String name;
    private final BigDecimal price;

    /**
     * @param price unit price in Rand with at most two decimal places (cents).
     *              Money is never rounded silently: a price like 9.999 is rejected.
     *              The stored price always has exactly two decimal places.
     */
    public Product(int id, String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (price == null) {
            throw new IllegalArgumentException("Product price must not be null");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Product price must not be negative");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Product price must have at most 2 decimal places, was: " + price);
        }
        this.id = id;
        this.name = name;
        this.price = price.setScale(2, RoundingMode.UNNECESSARY);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + '}';
    }
}
