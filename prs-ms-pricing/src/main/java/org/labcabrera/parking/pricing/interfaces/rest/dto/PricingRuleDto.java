package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PricingRule", description = "Pricing rule configuration")
public record PricingRuleDto(
    @Schema(description = "Pricing rule identifier", example = "018f4f3b-9c52-7d87-9f0b-7b2d8a4b8c12")
    UUID id,
    @Schema(description = "Human-readable pricing rule name", example = "Airport hybrid rate")
    String name,
    @Schema(description = "Billing strategy supported by the rule", example = "HYBRID")
    BillingType billingType,
    @Schema(description = "Base rate applied by generic pricing calculations")
    MoneyDto baseRate,
    @Schema(description = "Hourly rate used by hourly and short-slot dynamic pricing")
    MoneyDto hourlyRate,
    @Schema(description = "Daily rate used by daily and long-slot dynamic pricing")
    MoneyDto dailyRate,
    @Schema(description = "Tax rate as a decimal ratio from 0.0000 to 1.0000", example = "0.2100")
    BigDecimal taxRate,
    @Schema(description = "Lifecycle status of the pricing rule", example = "ACTIVE")
    PricingRuleStatus status,
    @Schema(description = "First instant when this rule is valid", example = "2026-06-01T00:00:00")
    LocalDateTime validFrom,
    @Schema(description = "Last instant when this rule is valid", example = "2026-12-31T23:59:59")
    LocalDateTime validTo,
    @Schema(description = "Creation timestamp", example = "2026-06-01T10:15:30")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2026-06-02T10:15:30")
    LocalDateTime updatedAt,
    @Schema(description = "Optimistic locking version", example = "1")
    Long version) {
}
