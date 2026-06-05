package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePricingRuleRequest(

    @NotBlank String name,

    @NotNull BillingType billingType,

    @NotNull @Valid MoneyDto baseRate,

    @Valid MoneyDto hourlyRate,

    @Valid MoneyDto dailyRate,

    @DecimalMin("0.0000") BigDecimal taxRate,

    PricingRuleStatus status,

    LocalDateTime validFrom,

    LocalDateTime validTo) {
}
