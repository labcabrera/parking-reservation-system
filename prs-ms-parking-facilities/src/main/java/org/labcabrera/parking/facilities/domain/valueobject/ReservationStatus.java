package org.labcabrera.parking.facilities.domain.valueobject;

public enum ReservationStatus {
    PENDING, HELD, CONFIRMED, CANCELLED, EXPIRED, FAILED;

    public boolean isTerminal() {
        return this == CONFIRMED || this == CANCELLED || this == EXPIRED || this == FAILED;
    }
}
