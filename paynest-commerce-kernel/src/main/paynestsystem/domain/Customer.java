package com.paynestsystem.domain;

/**
 * The person buying from the merchant. Kept intentionally small for
 * Capstone 1 — just enough to head a printed receipt.
 */
public class Customer {

    private final int id;
    private final String name;
    private final String email;

    public Customer(int id, String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email must not be blank");
        }
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
