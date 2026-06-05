package org.labcabrera.parking.ecommerce.domain.valueobject;

import java.math.BigDecimal;
import java.util.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record Money(

    @NotNull
    @PositiveOrZero
    BigDecimal amount,

    @NotBlank
    String currency) {

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
