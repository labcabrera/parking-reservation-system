package org.labcabrera.parking.pricing.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;

class PricingRuleTest {

    @Test
    void createsDailyPricingRule() {
        PricingRule rule = new PricingRule(
            UUID.randomUUID(),
            "Airport daily rate",
            BillingType.DAILY,
            new Money(new BigDecimal("5.00"), "EUR"),
            null,
            new Money(new BigDecimal("30.00"), "EUR"),
            new BigDecimal("0.2100"),
            PricingRuleStatus.ACTIVE,
            LocalDateTime.now(),
            null);

        assertThat(rule.getId()).isNotNull();
        assertThat(rule.getBillingType()).isEqualTo(BillingType.DAILY);
        assertThat(rule.getStatus()).isEqualTo(PricingRuleStatus.ACTIVE);
    }

    @Test
    void hourlyBillingRequiresHourlyRate() {
        assertThatThrownBy(() -> new PricingRule(
            UUID.randomUUID(),
            "Airport hourly rate",
            BillingType.HOURLY,
            new Money(new BigDecimal("5.00"), "EUR"),
            null,
            null,
            BigDecimal.ZERO,
            PricingRuleStatus.ACTIVE,
            null,
            null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hourlyRate is required");
    }
}
