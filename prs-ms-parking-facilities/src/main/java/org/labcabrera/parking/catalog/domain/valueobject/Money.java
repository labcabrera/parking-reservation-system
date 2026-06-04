package org.labcabrera.parking.catalog.domain.valueobject;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money euros(BigDecimal amount) {
        return new Money(amount, "EUR");
    }
}
