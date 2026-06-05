package org.labcabrera.parking.pricing.infrastructure.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pricing_rule")
@Getter
@Setter
@NoArgsConstructor
public class PricingRuleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type", nullable = false, length = 20)
    private BillingType billingType;

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "hourly_amount", precision = 10, scale = 2)
    private BigDecimal hourlyAmount;

    @Column(name = "hourly_currency", length = 3)
    private String hourlyCurrency;

    @Column(name = "daily_amount", precision = 10, scale = 2)
    private BigDecimal dailyAmount;

    @Column(name = "daily_currency", length = 3)
    private String dailyCurrency;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PricingRuleStatus status;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
