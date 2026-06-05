package org.labcabrera.parking.ecommerce.domain.valueobject;

public enum PaymentMethodStatus {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
