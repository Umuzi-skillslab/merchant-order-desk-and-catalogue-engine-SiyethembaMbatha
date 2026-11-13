package com.paynestsystem.payment;

import java.math.BigDecimal;

import com.paynestsystem.util.QuietConsole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.paynestsystem.util.ConsoleCapture.capture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuietConsole.class)
class PaymentMethodTest {

    private static BigDecimal rand(String amount) {
        return new BigDecimal(amount);
    }

    /** A rail that always declines, to exercise the failure path. */
    private static PaymentMethod decliningRail() {
        return new PaymentMethod() {
            @Override
            public boolean processPayment(BigDecimal amount) {
                return false;
            }

            @Override
            public String getPaymentType() {
                return "DECLINED_TEST_RAIL";
            }
        };
    }

    @Test
    void checkoutCanUseDifferentPaymentMethods() {
        PaymentMethod card = new CardPayment();
        PaymentMethod wallet = new WalletPayment();

        assertTrue(card.processPayment(rand("12400.00")));
        assertTrue(wallet.processPayment(rand("12400.00")));
        assertEquals("CARD", card.getPaymentType());
        assertEquals("WALLET", wallet.getPaymentType());
    }

    @Test
    void eftPayment_processesAndLabelsCorrectly() {
        PaymentMethod eft = new EftPayment();

        assertTrue(eft.processPayment(rand("5000.00")));
        assertEquals("EFT", eft.getPaymentType());
    }

    @Test
    void eachRail_printsItsOwnMessageWithAmountInSameFormat() {
        String card = capture(() -> new CardPayment().processPayment(rand("13050.00")));
        String eft = capture(() -> new EftPayment().processPayment(rand("13050.00")));
        String wallet = capture(() -> new WalletPayment().processPayment(rand("13050.00")));

        assertTrue(card.contains("card") && card.contains("R13,050.00"));
        assertTrue(eft.contains("EFT") && eft.contains("R13,050.00"));
        assertTrue(wallet.contains("wallet") && wallet.contains("R13,050.00"));
    }

    @Test
    void paymentProcessor_reportsSuccessForAnyPaymentMethod() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();

        assertTrue(paymentProcessor.processPayment(new CardPayment(), rand("12000.00")));
        assertTrue(paymentProcessor.processPayment(new EftPayment(), rand("350.00")));
        assertTrue(paymentProcessor.processPayment(new WalletPayment(), rand("13050.00")));
    }

    @Test
    void paymentProcessor_successMessageNamesRailAndAmount() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();

        String output = capture(() -> paymentProcessor.processPayment(new EftPayment(), rand("13050.00")));

        assertTrue(output.contains("Payment successful via EFT"));
        assertTrue(output.contains("Amount: R13,050.00"));
    }

    @Test
    void paymentProcessor_reportsFailureWhenRailDeclines() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        boolean[] result = new boolean[1];

        String output = capture(() -> result[0] = paymentProcessor.processPayment(decliningRail(), rand("500.00")));

        assertFalse(result[0]);
        assertTrue(output.contains("Payment failed via DECLINED_TEST_RAIL"));
        assertFalse(output.contains("Payment successful"));
    }

    @Test
    void paymentProcessor_rejectsNullPaymentMethod() {
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentProcessor().processPayment(null, rand("100.00")));
    }

    @Test
    void paymentProcessor_rejectsNullAndNegativeAmounts() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        PaymentMethod card = new CardPayment();

        assertThrows(IllegalArgumentException.class, () -> paymentProcessor.processPayment(card, null));
        assertThrows(IllegalArgumentException.class, () -> paymentProcessor.processPayment(card, rand("-1.00")));
    }
}
