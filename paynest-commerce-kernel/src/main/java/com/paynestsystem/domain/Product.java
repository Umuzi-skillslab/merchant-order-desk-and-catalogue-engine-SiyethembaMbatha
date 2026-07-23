package com.paynestsystem.domain;

public class Product {

    private final int id;
    private final String name;
    private final double price;

    public Product(int id, String name, double price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Product price must not be negative");
        }
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + '}';
    }
}
