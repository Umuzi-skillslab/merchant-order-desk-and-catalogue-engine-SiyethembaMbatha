package com.paynestsystem.domain;

import java.math.BigDecimal;

import com.paynestsystem.payment.CardPayment;
import com.paynestsystem.payment.PaymentMethod;
import com.paynestsystem.payment.PaymentProcessor;
import com.paynestsystem.util.QuietConsole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.paynestsystem.util.ConsoleCapture.capture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Business rules about when an order may be paid and changed. */
@ExtendWith(QuietConsole.class)
class OrderLifecycleTest {

    private final Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");
    private final Product laptop = new Product(1, "Laptop", new BigDecimal("12000.00"));
    private final Product mouse = new Product(2, "Wireless Mouse", new BigDecimal("350.00"));

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
    void newOrder_startsUnpaid() {
        assertFalse(new Order(1, customer).isPaid());
    }

    @Test
    void successfulCheckout_marksOrderPaid() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        capture(() -> order.checkout(new CardPayment()));

        assertTrue(order.isPaid());
    }

    @Test
    void declinedCheckout_leavesOrderUnpaidAndRetryable() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        capture(() -> order.checkout(decliningRail()));
        assertFalse(order.isPaid());

        boolean[] retry = new boolean[1];
        capture(() -> retry[0] = order.checkout(new CardPayment()));
        assertTrue(retry[0]);
        assertTrue(order.isPaid());
    }

    @Test
    void paidOrder_cannotBeChargedAgain() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);
        capture(() -> order.checkout(new CardPayment()));

        int[] charges = new int[1];
        PaymentProcessor countingProcessor = new PaymentProcessor() {
            @Override
            public boolean processPayment(PaymentMethod method, BigDecimal amount) {
                charges[0]++;
                return true;
            }
        };

        assertThrows(IllegalStateException.class,
                () -> order.checkout(new CardPayment(), countingProcessor));
        assertEquals(0, charges[0]);
    }

    @Test
    void paidOrder_cannotBeChanged() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);
        capture(() -> order.checkout(new CardPayment()));

        assertThrows(IllegalStateException.class, () -> order.addItem(mouse, 1));
        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("12000.00"), order.calculateTotal());
    }

    @Test
    void emptyOrder_cannotBeCheckedOutOrCharged() {
        Order order = new Order(1, customer);

        int[] charges = new int[1];
        PaymentMethod countingRail = new PaymentMethod() {
            @Override
            public boolean processPayment(BigDecimal amount) {
                charges[0]++;
                return true;
            }

            @Override
            public String getPaymentType() {
                return "COUNTING_TEST_RAIL";
            }
        };

        assertThrows(IllegalStateException.class, () -> order.checkout(countingRail));
        assertEquals(0, charges[0]);
        assertFalse(order.isPaid());
    }
}
