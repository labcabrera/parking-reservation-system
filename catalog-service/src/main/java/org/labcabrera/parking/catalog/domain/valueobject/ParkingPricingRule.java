package org.labcabrera.parking.catalog.domain.valueobject;

import java.math.BigDecimal;

public record ParkingPricingRule(
    String externalPricingId,
    BigDecimal estimatedDailyPrice
) {}
