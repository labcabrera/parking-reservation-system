package org.labcabrera.parking.reservation.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency must not be blank");
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
}
