package org.labcabrera.parking.pricing.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

import lombok.Getter;

@Getter
public class PricingRule {

    private final UUID id;
    private UUID facilityId;
    private String name;
    private BillingType billingType;
    private Money baseRate;
    private Money hourlyRate;
    private Money dailyRate;
    private BigDecimal taxRate;
    private PricingRuleStatus status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public PricingRule(
        UUID facilityId,
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
            facilityId,
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
        UUID facilityId,
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
        validate(facilityId, name, billingType, baseRate, hourlyRate, dailyRate, taxRate, validFrom, validTo);
        this.id = id != null ? id : UUID.randomUUID();
        this.facilityId = facilityId;
        this.name = name;
        this.billingType = billingType;
        this.baseRate = baseRate;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
        this.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        this.status = status != null ? status : PricingRuleStatus.ACTIVE;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt;
        this.version = version;
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
        validate(facilityId, name, billingType, baseRate, hourlyRate, dailyRate, taxRate, validFrom, validTo);
        this.name = name;
        this.billingType = billingType;
        this.baseRate = baseRate;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
        this.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        this.status = status != null ? status : PricingRuleStatus.ACTIVE;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = PricingRuleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private static void validate(
        UUID facilityId,
        String name,
        BillingType billingType,
        Money baseRate,
        Money hourlyRate,
        Money dailyRate,
        BigDecimal taxRate,
        LocalDateTime validFrom,
        LocalDateTime validTo) {
        if (facilityId == null) {
            throw new IllegalArgumentException("facilityId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (billingType == null) {
            throw new IllegalArgumentException("billingType is required");
        }
        if (baseRate == null) {
            throw new IllegalArgumentException("baseRate is required");
        }
        if ((billingType == BillingType.HOURLY || billingType == BillingType.HYBRID) && hourlyRate == null) {
            throw new IllegalArgumentException("hourlyRate is required for " + billingType);
        }
        if ((billingType == BillingType.DAILY || billingType == BillingType.HYBRID) && dailyRate == null) {
            throw new IllegalArgumentException("dailyRate is required for " + billingType);
        }
        if (taxRate != null && taxRate.signum() < 0) {
            throw new IllegalArgumentException("taxRate must be zero or greater");
        }
        if (validFrom != null && validTo != null && !validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("validTo must be after validFrom");
        }
    }
}
