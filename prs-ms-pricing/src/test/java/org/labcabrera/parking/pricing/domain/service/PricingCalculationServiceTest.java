package org.labcabrera.parking.pricing.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;
import org.labcabrera.parking.pricing.domain.valueobject.SlotOccupancy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD: These tests must FAIL before PricingCalculationService is implemented (T026).
 */
class PricingCalculationServiceTest {

    private final PricingCalculationService service = new PricingCalculationService();

    @Test
    @DisplayName("confirmedPrice = baseRatePerDay x days for a 2-day booking")
    void twoDayBooking() {
        BigDecimal baseRate = new BigDecimal("30.00");
        int days = 2;

        BigDecimal result = service.calculate(baseRate, days);

        assertThat(result).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    @DisplayName("same-day booking counts as 1 day")
    void sameDayCountsAsOneDay() {
        BigDecimal baseRate = new BigDecimal("25.00");
        int days = 0; // same-day: 0 DAYS between checkIn/checkOut

        BigDecimal result = service.calculate(baseRate, days);

        assertThat(result).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("multi-week booking — 14 days")
    void multiWeekBooking() {
        BigDecimal baseRate = new BigDecimal("20.00");
        int days = 14;

        BigDecimal result = service.calculate(baseRate, days);

        assertThat(result).isEqualByComparingTo(new BigDecimal("280.00"));
    }

    @Test
    @DisplayName("zero rate results in zero price")
    void zeroRateResultsInZeroPrice() {
        BigDecimal baseRate = BigDecimal.ZERO;
        int days = 5;

        BigDecimal result = service.calculate(baseRate, days);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("null baseRate throws IllegalArgumentException")
    void nullBaseRateThrows() {
        assertThatThrownBy(() -> service.calculate(null, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("negative days throws IllegalArgumentException")
    void negativeDaysThrows() {
        assertThatThrownBy(() -> service.calculate(new BigDecimal("10.00"), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("dynamic rate uses pricing rule slot rate, slot count, and average occupancy multiplier")
    void dynamicRateUsesSlotOccupancy() {
        var result = service.calculateDynamicRate(
            pricingRule(),
            PricingSlotType.SHORT,
            List.of(new SlotOccupancy(new BigDecimal("0.5000")), new SlotOccupancy(new BigDecimal("1.0000"))));

        assertThat(result.slotType()).isEqualTo(PricingSlotType.SHORT);
        assertThat(result.parkingRate()).isEqualByComparingTo(new BigDecimal("10.0000"));
        assertThat(result.slotCount()).isEqualTo(2);
        assertThat(result.averageOccupancyRate()).isEqualByComparingTo(new BigDecimal("0.7500"));
        assertThat(result.averageOccupancyMultiplier()).isEqualByComparingTo(new BigDecimal("1.7500"));
        assertThat(result.price().baseAmount()).isEqualByComparingTo(new BigDecimal("35.0000"));
        assertThat(result.price().taxAmount()).isEqualByComparingTo(new BigDecimal("7.3500"));
        assertThat(result.price().totalAmount()).isEqualByComparingTo(new BigDecimal("42.3500"));
        assertThat(result.price().currency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("dynamic rate rejects empty slots")
    void dynamicRateRejectsEmptySlots() {
        assertThatThrownBy(() -> service.calculateDynamicRate(pricingRule(), PricingSlotType.LONG, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("slots must not be empty");
    }

    private PricingRule pricingRule() {
        return new PricingRule(
            "Airport hybrid rate",
            BillingType.HYBRID,
            new Money(new BigDecimal("5.00"), "EUR"),
            new Money(new BigDecimal("20.00"), "EUR"),
            new Money(new BigDecimal("50.00"), "EUR"),
            new BigDecimal("0.2100"),
            PricingRuleStatus.ACTIVE,
            LocalDateTime.now(),
            null);
    }
}
