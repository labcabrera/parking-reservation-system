package org.labcabrera.parking.facilities.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReservationStatusTest {

    @Test
    void confirmedIsNotTerminalUntilPaymentIsResolved() {
        assertFalse(ReservationStatus.CONFIRMED.isTerminal());
        assertTrue(ReservationStatus.PAYMENT_EXPIRED.isTerminal());
        assertTrue(ReservationStatus.CANCELLED.isTerminal());
        assertTrue(ReservationStatus.EXPIRED.isTerminal());
        assertTrue(ReservationStatus.FAILED.isTerminal());
    }
}
