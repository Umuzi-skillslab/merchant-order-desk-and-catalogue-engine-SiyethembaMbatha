package com.paynestsystem.payment;

import java.math.BigDecimal;
import com.paynestsystem.domain.Customer;
import com.paynestsystem.domain.Order;
import com.paynestsystem.domain.Product;
import com.paynestsystem.util.Money;

import com.paynestsystem.util.QuietConsole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.paynestsystem.util.ConsoleCapture.capture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The brief's design check: "imagine adding BuyNowPayLaterPayment".
 * The rail below is ONE new class that lives only in this test. Order,
 * PaymentProcessor and every existing rail are used exactly as they are,
 * which is the proof that a new rail needs a new class plus wiring, and
 * no edits to checkout or to the order total calculation.
 */
@ExtendWith(QuietConsole.class)
class NewRailExtensibilityTest {

    static class BuyNowPayLaterPayment implements PaymentMethod {
        BigDecimal lastAmount;

        @Override
        public boolean processPayment(BigDecimal amount) {
            lastAmount = amount;
            System.out.println("Processing buy-now-pay-later plan for " + Money.formatRand(amount));
            return true;
        }

        @Override
        public String getPaymentType() {
            return "BNPL";
        }
    }

    @Test
    void newRail_worksThroughExistingCheckoutUnchanged() {
        Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");
        Order order = new Order(1, customer);
        order.addItem(new Product(1, "Laptop", new BigDecimal("12000.00")), 1);
        order.addItem(new Product(2, "Wireless Mouse", new BigDecimal("350.00")), 3);

        BuyNowPayLaterPayment bnpl = new BuyNowPayLaterPayment();
        String output = capture(() -> assertTrue(order.checkout(bnpl)));

        assertEquals(new BigDecimal("13050.00"), bnpl.lastAmount);
        assertTrue(output.contains("Payment successful via BNPL"));
        assertTrue(output.contains("Order #1 complete for Thandiwe Nkosi via BNPL (R13,050.00)"));
    }
}
