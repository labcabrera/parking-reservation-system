package org.labcabrera.parking.ecommerce.domain.valueobject;

public enum PaymentAttemptStatus {
    PENDING, PROCESSING, SUCCEEDED, FAILED;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED;
    }
}
