package org.labcabrera.parking.catalog.domain.valueobjects;

public enum ReservationStatus {
    PENDING,
    HELD,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    FAILED;

    public boolean isTerminal() {
        return this == CONFIRMED || this == CANCELLED || this == EXPIRED || this == FAILED;
    }
}
