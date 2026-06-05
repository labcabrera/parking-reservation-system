package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

public record PricingRuleDto(
    UUID id,
    UUID facilityId,
    String name,
    BillingType billingType,
    MoneyDto baseRate,
    MoneyDto hourlyRate,
    MoneyDto dailyRate,
    BigDecimal taxRate,
    PricingRuleStatus status,
    LocalDateTime validFrom,
    LocalDateTime validTo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version) {
}
