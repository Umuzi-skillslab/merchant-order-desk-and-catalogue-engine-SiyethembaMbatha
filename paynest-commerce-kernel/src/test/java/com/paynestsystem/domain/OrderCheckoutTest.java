package com.paynestsystem.domain;

import java.math.BigDecimal;

import com.paynestsystem.payment.CardPayment;
import com.paynestsystem.payment.EftPayment;
import com.paynestsystem.payment.PaymentMethod;
import com.paynestsystem.payment.PaymentProcessor;
import com.paynestsystem.payment.WalletPayment;
import com.paynestsystem.util.QuietConsole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.paynestsystem.util.ConsoleCapture.capture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuietConsole.class)
class OrderCheckoutTest {

    private final Customer customer = new Customer(1, "Thandiwe Nkosi", "thandiwe@example.co.za");
    private final Product laptop = new Product(1, "Laptop", new BigDecimal("12000.00"));
    private final Product mouse = new Product(2, "Wireless Mouse", new BigDecimal("350.00"));

    private Order orderWithLaptopAndMice() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);
        order.addItem(mouse, 3);
        return order;
    }

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
    void checkout_passesOrderTotalToPaymentMethod() {
        Order order = orderWithLaptopAndMice();

        BigDecimal[] amountCharged = new BigDecimal[1];
        PaymentMethod capturingPaymentMethod = new PaymentMethod() {
            @Override
            public boolean processPayment(BigDecimal amount) {
                amountCharged[0] = amount;
                return true;
            }

            @Override
            public String getPaymentType() {
                return "TEST";
            }
        };

        boolean success = order.checkout(capturingPaymentMethod);

        assertTrue(success);
        assertEquals(order.calculateTotal(), amountCharged[0]);
        assertEquals(new BigDecimal("13050.00"), amountCharged[0]);
    }

    @Test
    void checkout_worksWithDifferentPaymentMethodImplementations() {
        Order cardOrder = new Order(1, customer);
        cardOrder.addItem(laptop, 1);

        Order eftOrder = new Order(2, customer);
        eftOrder.addItem(mouse, 3);

        Order walletOrder = new Order(3, customer);
        walletOrder.addItem(mouse, 1);

        assertTrue(cardOrder.checkout(new CardPayment()));
        assertTrue(eftOrder.checkout(new EftPayment()));
        assertTrue(walletOrder.checkout(new WalletPayment()));
    }

    @Test
    void checkout_rejectsNullPaymentMethod() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        assertThrows(IllegalArgumentException.class, () -> order.checkout(null));
    }

    @Test
    void checkout_rejectsNullPaymentProcessor() {
        Order order = new Order(1, customer);
        order.addItem(laptop, 1);

        assertThrows(IllegalArgumentException.class,
                () -> order.checkout(new CardPayment(), null));
    }

    @Test
    void checkout_usesTheSuppliedProcessorExactlyOnceWithTheOrderTotal() {
        Order order = orderWithLaptopAndMice();
        PaymentMethod rail = new EftPayment();

        int[] calls = new int[1];
        BigDecimal[] amountSeen = new BigDecimal[1];
        PaymentMethod[] methodSeen = new PaymentMethod[1];
        PaymentProcessor recordingProcessor = new PaymentProcessor() {
            @Override
            public boolean processPayment(PaymentMethod method, BigDecimal amount) {
                calls[0]++;
                amountSeen[0] = amount;
                methodSeen[0] = method;
                return true;
            }
        };

        boolean success = order.checkout(rail, recordingProcessor);

        assertTrue(success);
        assertEquals(1, calls[0]);
        assertEquals(new BigDecimal("13050.00"), amountSeen[0]);
        assertSame(rail, methodSeen[0]);
    }

    @Test
    void checkout_successOutputShowsSummaryRailAmountAndCompletion() {
        Order order = orderWithLaptopAndMice();

        String output = capture(() -> order.checkout(new CardPayment()));

        assertTrue(output.contains("Thandiwe Nkosi"));
        assertTrue(output.contains("Grand Total"));
        assertTrue(output.contains("Processing card payment for R13,050.00"));
        assertTrue(output.contains("Payment successful via CARD"));
        assertTrue(output.contains("Order #1 complete for Thandiwe Nkosi via CARD (R13,050.00)"));
    }

    @Test
    void checkout_returnsFalseAndReportsFailureWhenPaymentIsDeclined() {
        Order order = orderWithLaptopAndMice();
        boolean[] result = new boolean[] {true};

        String output = capture(() -> result[0] = order.checkout(decliningRail()));

        assertFalse(result[0]);
        assertTrue(output.contains("Payment failed via DECLINED_TEST_RAIL"));
        assertTrue(output.contains("Order #1 could not be completed for Thandiwe Nkosi via DECLINED_TEST_RAIL"));
        assertFalse(output.contains("complete for"));
    }

    @Test
    void checkout_doesNotChangeTheOrderWhenDeclined() {
        Order order = orderWithLaptopAndMice();

        capture(() -> order.checkout(decliningRail()));

        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("13050.00"), order.calculateTotal());
    }

    @Test
    void checkout_chargesTheExactDecimalTotal() {
        // With double, 0.10 * 3 is 0.30000000000000004. BigDecimal is exact.
        Order order = new Order(1, customer);
        order.addItem(new Product(9, "Sweet", new BigDecimal("0.10")), 3);

        BigDecimal[] charged = new BigDecimal[1];
        PaymentMethod rail = new PaymentMethod() {
            @Override
            public boolean processPayment(BigDecimal amount) {
                charged[0] = amount;
                return true;
            }

            @Override
            public String getPaymentType() {
                return "TEST";
            }
        };

        assertTrue(order.checkout(rail));
        assertEquals(new BigDecimal("0.30"), charged[0]);
    }
}
