package org.labcabrera.parking.catalog.domain.valueobjects;

import java.math.BigDecimal;

public record ParkingPricingRule(
    String externalPricingId,
    BigDecimal estimatedDailyPrice
) {}
