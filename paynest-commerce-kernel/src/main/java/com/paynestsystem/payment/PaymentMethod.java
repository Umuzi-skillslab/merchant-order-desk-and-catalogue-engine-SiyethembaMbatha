package com.paynestsystem.payment;

import java.math.BigDecimal;

/**
 * "How the customer pays." This is the single contract checkout depends on.
 * Concrete rails (card, EFT, wallet, ...) implement it; nothing else in the
 * system needs to know which one is in use.
 *
 * <p>Contract assumptions:
 * <ul>
 *   <li>{@code amount} is an exact Rand amount ({@link BigDecimal}, two decimal
 *       places), already calculated by the order. A rail must charge exactly that
 *       amount (no silent discounts or fees).</li>
 *   <li>{@link #processPayment(BigDecimal)} returns {@code true} if the charge
 *       succeeded and {@code false} if it was declined. Failure is a normal
 *       outcome, reported by the return value rather than by an exception.</li>
 *   <li>{@link #getPaymentType()} is a stable, human-readable label
 *       (e.g. {@code "CARD"}) suitable for logs and receipts.</li>
 * </ul>
 */
public interface PaymentMethod {

    boolean processPayment(BigDecimal amount);

    String getPaymentType();
}
