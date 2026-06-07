package org.labcabrera.parking.pricing.domain.valueobject;

import java.math.BigDecimal;

public record DynamicPriceBreakdown(
    BigDecimal baseAmount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String currency) {
}
