package com.paynestsystem.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    @Test
    void validCustomer_exposesItsFields() {
        Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");

        assertEquals(1, customer.getId());
        assertEquals("Thandiwe Nkosi", customer.getName());
        assertEquals("thandiwe@example.co.za", customer.getEmail());
    }

    @Test
    void constructor_rejectsBlankOrNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(1, null, "a@b.co"));
        assertThrows(IllegalArgumentException.class, () -> new Customer(1, " ", "a@b.co"));
    }

    @Test
    void constructor_rejectsBlankOrNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(1, "Thandiwe", null));
        assertThrows(IllegalArgumentException.class, () -> new Customer(1, "Thandiwe", ""));
    }
}
