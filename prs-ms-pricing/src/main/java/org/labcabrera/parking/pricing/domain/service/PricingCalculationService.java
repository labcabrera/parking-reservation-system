package org.labcabrera.parking.pricing.domain.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * MVP pricing formula: confirmedPrice = baseRatePerDay × max(days, 1)
 */
@Service
public class PricingCalculationService {

    public BigDecimal calculate(BigDecimal baseRatePerDay, int days) {
        if (baseRatePerDay == null) {
            throw new IllegalArgumentException("baseRatePerDay must not be null");
        }
        if (days < 0) {
            throw new IllegalArgumentException("days must be >= 0");
        }
        int effectiveDays = Math.max(days, 1);
        return baseRatePerDay.multiply(BigDecimal.valueOf(effectiveDays));
    }
}
