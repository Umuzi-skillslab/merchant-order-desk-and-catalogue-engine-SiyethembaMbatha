package com.paynestsystem.payment;

import java.math.BigDecimal;

import com.paynestsystem.util.Money;

/**
 * Runs a payment through whichever {@link PaymentMethod} it is given and
 * reports the outcome on the console. It works only with the interface, so a
 * new rail never requires a change here.
 */
public class PaymentProcessor {

    public boolean processPayment(PaymentMethod method, BigDecimal amount) {
        if (method == null) {
            throw new IllegalArgumentException("PaymentProcessor requires a payment method");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be a non-negative number, was: " + amount);
        }

        boolean success = method.processPayment(amount);

        if (success) {
            System.out.println("Payment successful via " + method.getPaymentType());
            System.out.println("Amount: " + Money.formatRand(amount));
        } else {
            System.out.println("Payment failed via " + method.getPaymentType());
        }

        return success;
    }
}
