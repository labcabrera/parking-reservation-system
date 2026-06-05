package org.labcabrera.parking.ecommerce.domain.valueobject;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_IN_PROGRESS,
    PAID,
    EXPIRED,
    CANCELLED,
    PAYMENT_FAILED;

    public boolean isTerminal() {
        return this == PAID || this == EXPIRED || this == CANCELLED || this == PAYMENT_FAILED;
    }
}
