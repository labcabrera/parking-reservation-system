package org.labcabrera.parking.facilities.domain.valueobject;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ParkingPricingRule(
    @NotNull UUID externalPricingId,

    @NotNull @DecimalMin(value = "0.01") BigDecimal estimatedDailyPrice) {
}
