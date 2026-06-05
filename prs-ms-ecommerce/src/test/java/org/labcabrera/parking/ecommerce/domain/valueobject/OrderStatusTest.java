package org.labcabrera.parking.ecommerce.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void identifiesTerminalStates() {
        assertTrue(OrderStatus.PAID.isTerminal());
        assertTrue(OrderStatus.EXPIRED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertTrue(OrderStatus.PAYMENT_FAILED.isTerminal());
        assertFalse(OrderStatus.PENDING_PAYMENT.isTerminal());
        assertFalse(OrderStatus.PAYMENT_IN_PROGRESS.isTerminal());
    }
}
