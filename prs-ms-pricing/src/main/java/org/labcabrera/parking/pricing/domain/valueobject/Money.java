package org.labcabrera.parking.pricing.domain.valueobject;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be zero or greater");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        Currency.getInstance(currency);
    }
}
