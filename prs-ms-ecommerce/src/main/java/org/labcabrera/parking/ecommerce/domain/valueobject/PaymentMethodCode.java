package org.labcabrera.parking.ecommerce.domain.valueobject;

import java.util.Locale;

public record PaymentMethodCode(String value) {

    private static final int MAX_LENGTH = 40;

    public PaymentMethodCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("payment method code is required");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("payment method code cannot exceed " + MAX_LENGTH + " characters");
        }
        if (!value.matches("[A-Z0-9_]+")) {
            throw new IllegalArgumentException("payment method code must contain only uppercase letters, digits or underscore");
        }
    }
}
