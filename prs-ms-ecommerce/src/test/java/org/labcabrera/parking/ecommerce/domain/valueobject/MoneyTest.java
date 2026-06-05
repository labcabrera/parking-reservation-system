package org.labcabrera.parking.ecommerce.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void createsMoneyWithValidCurrency() {
        var money = new Money(new BigDecimal("37.50"), "EUR");

        assertEquals(new BigDecimal("37.50"), money.amount());
        assertEquals("EUR", money.currency());
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-0.01"), "EUR"));
    }

    @Test
    void rejectsInvalidCurrency() {
        assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.ONE, "EURO"));
    }
}
