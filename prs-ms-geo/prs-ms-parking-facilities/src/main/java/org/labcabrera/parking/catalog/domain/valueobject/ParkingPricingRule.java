package org.labcabrera.parking.catalog.domain.valueobject;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParkingPricingRule(
    @NotBlank
    String externalPricingId,
    
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal estimatedDailyPrice
) {}
