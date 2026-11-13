package com.paynestsystem.payment;

import java.math.BigDecimal;

import com.paynestsystem.util.Money;

public class CardPayment implements PaymentMethod {

    @Override
    public boolean processPayment(BigDecimal amount) {
        System.out.println("Processing card payment for " + Money.formatRand(amount));
        return true; // Simulated payment for the capstone demo.
    }

    @Override
    public String getPaymentType() {
        return "CARD";
    }
}
