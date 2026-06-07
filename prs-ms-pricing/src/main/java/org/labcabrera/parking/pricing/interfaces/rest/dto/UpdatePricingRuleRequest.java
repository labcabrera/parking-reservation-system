package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdatePricingRuleRequest", description = "Request to replace a pricing rule configuration")
public record UpdatePricingRuleRequest(

    @Schema(description = "Human-readable pricing rule name", example = "Airport hybrid rate")
    @NotBlank
    String name,

    @Schema(description = "Billing strategy supported by the rule", example = "HYBRID")
    @NotNull
    BillingType billingType,

    @Schema(description = "Base rate applied by generic pricing calculations")
    @NotNull
    @Valid
    MoneyDto baseRate,

    @Schema(description = "Hourly rate used by hourly and short-slot dynamic pricing")
    @Valid
    MoneyDto hourlyRate,

    @Schema(description = "Daily rate used by daily and long-slot dynamic pricing")
    @Valid
    MoneyDto dailyRate,

    @Schema(description = "Tax rate as a decimal ratio from 0.0000 to 1.0000. Example: 0.2100 means 21%.", example = "0.2100")
    @DecimalMin("0.0000")
    @DecimalMax("1.0000")
    @Digits(integer = 1, fraction = 4)
    BigDecimal taxRate,

    @Schema(description = "Lifecycle status of the pricing rule", example = "ACTIVE")
    PricingRuleStatus status,

    @Schema(description = "First instant when this rule is valid", example = "2026-06-01T00:00:00")
    LocalDateTime validFrom,

    @Schema(description = "Last instant when this rule is valid. Null means no configured end date.", example = "2026-12-31T23:59:59")
    LocalDateTime validTo) {
}
