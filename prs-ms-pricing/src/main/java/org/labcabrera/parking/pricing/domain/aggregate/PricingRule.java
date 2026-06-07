package org.labcabrera.parking.pricing.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PricingRule {

    private static final int TAX_RATE_SCALE = 4;

    @NotNull
    private final UUID id;

    @NotBlank
    private String name;

    @NotNull
    private BillingType billingType;

    @NotNull
    @Valid
    private Money baseRate;

    @Valid
    private Money hourlyRate;

    @Valid
    private Money dailyRate;

    @NotNull
    @DecimalMin("0.0000")
    @DecimalMax("1.0000")
    @Digits(integer = 1, fraction = 4)
    private BigDecimal taxRate;

    @NotNull
    private PricingRuleStatus status;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private Long version;

    public PricingRule(
        String name,
        BillingType billingType,
        Money baseRate,
        Money hourlyRate,
        Money dailyRate,
        BigDecimal taxRate,
        PricingRuleStatus status,
        LocalDateTime validFrom,
        LocalDateTime validTo) {
        this(
            UUID.randomUUID(),
            name,
            billingType,
            baseRate,
            hourlyRate,
            dailyRate,
            taxRate,
            status,
            validFrom,
            validTo,
            LocalDateTime.now(),
            null,
            null);
    }

    public PricingRule(
        UUID id,
        String name,
        BillingType billingType,
        Money baseRate,
        Money hourlyRate,
        Money dailyRate,
        BigDecimal taxRate,
        PricingRuleStatus status,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.billingType = billingType;
        this.baseRate = baseRate;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
        this.taxRate = normalizeTaxRate(taxRate);
        this.status = status != null ? status : PricingRuleStatus.ACTIVE;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt;
        this.version = version;
        validateBusinessRules();
    }

    public void update(
        String name,
        BillingType billingType,
        Money baseRate,
        Money hourlyRate,
        Money dailyRate,
        BigDecimal taxRate,
        PricingRuleStatus status,
        LocalDateTime validFrom,
        LocalDateTime validTo) {
        this.name = name;
        this.billingType = billingType;
        this.baseRate = baseRate;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
        this.taxRate = normalizeTaxRate(taxRate);
        this.status = status != null ? status : PricingRuleStatus.ACTIVE;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.updatedAt = LocalDateTime.now();
        validateBusinessRules();
    }

    public void deactivate() {
        this.status = PricingRuleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateBusinessRules() {
        if ((billingType == BillingType.HOURLY || billingType == BillingType.HYBRID) && hourlyRate == null) {
            throw new IllegalArgumentException("hourlyRate is required for " + billingType);
        }
        if ((billingType == BillingType.DAILY || billingType == BillingType.HYBRID) && dailyRate == null) {
            throw new IllegalArgumentException("dailyRate is required for " + billingType);
        }
        if (validFrom != null && validTo != null && !validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("validTo must be after validFrom");
        }
    }

    private static BigDecimal normalizeTaxRate(BigDecimal taxRate) {
        if (taxRate == null) {
            return BigDecimal.ZERO.setScale(TAX_RATE_SCALE);
        }
        if (taxRate.scale() > TAX_RATE_SCALE) {
            return taxRate;
        }
        return taxRate.setScale(TAX_RATE_SCALE);
    }
}
