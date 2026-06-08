package org.labcabrera.parking.facilities.domain.valueobject;

public enum ReservationStatus {
    PENDING, HELD, CONFIRMED, PAYMENT_EXPIRED, CANCELLED, EXPIRED, FAILED;

    public boolean isTerminal() {
        return this == PAYMENT_EXPIRED || this == CANCELLED || this == EXPIRED || this == FAILED;
    }
}
