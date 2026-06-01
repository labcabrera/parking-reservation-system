package org.labcabrera.parking.pricing.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
}
