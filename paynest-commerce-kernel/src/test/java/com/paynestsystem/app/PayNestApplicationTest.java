package com.paynestsystem.app;

import org.junit.jupiter.api.Test;

import static com.paynestsystem.util.ConsoleCapture.capture;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The brief's demo check: summary, payment rail, amount and completion all appear. */
class PayNestApplicationTest {

    @Test
    void demo_showsSummaryRailAmountAndOrderCompletion() {
        String output = capture(() -> PayNestApplication.main(new String[0]));

        assertTrue(output.contains("Order #1 for Thandiwe Nkosi"));
        assertTrue(output.contains("Laptop"));
        assertTrue(output.contains("Wireless Mouse"));
        assertTrue(output.contains("Grand Total"));
        assertTrue(output.contains("Processing card payment for R13,050.00"));
        assertTrue(output.contains("Payment successful via CARD"));
        assertTrue(output.contains("Order #1 complete for Thandiwe Nkosi via CARD (R13,050.00)"));
    }
}
