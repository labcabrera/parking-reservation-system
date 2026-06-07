package org.labcabrera.parking.pricing.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.valueobject.DynamicPriceBreakdown;
import org.labcabrera.parking.pricing.domain.valueobject.DynamicPricingResult;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;
import org.labcabrera.parking.pricing.domain.valueobject.SlotOccupancy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

/**
 * MVP pricing formula: confirmedPrice = baseRatePerDay × max(days, 1)
 */
@Service
@Validated
public class PricingCalculationService {

    private static final int RATE_SCALE = 4;
    private static final BigDecimal OCCUPANCY_BASE_MULTIPLIER = BigDecimal.ONE;

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

    public DynamicPricingResult calculateDynamicRate(
        @Valid PricingRule pricingRule,
        PricingSlotType slotType,
        List<SlotOccupancy> slots) {

        if (pricingRule == null) {
            throw new IllegalArgumentException("pricingRule is required");
        }
        if (slotType == null) {
            throw new IllegalArgumentException("slotType is required");
        }
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("slots must not be empty");
        }

        Money slotRate = resolveSlotRate(pricingRule, slotType);
        BigDecimal parkingRate = slotRate.amount();
        BigDecimal slotCount = BigDecimal.valueOf(slots.size());
        BigDecimal totalOccupancyRate = slots.stream()
            .map(SlotOccupancy::occupancyRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageOccupancyRate = totalOccupancyRate.divide(slotCount, RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal averageOccupancyMultiplier = OCCUPANCY_BASE_MULTIPLIER.add(averageOccupancyRate);
        BigDecimal baseAmount = parkingRate
            .multiply(slotCount)
            .multiply(averageOccupancyMultiplier)
            .setScale(RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal taxAmount = baseAmount
            .multiply(pricingRule.getTaxRate())
            .setScale(RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(taxAmount).setScale(RATE_SCALE, RoundingMode.HALF_UP);

        return new DynamicPricingResult(
            pricingRule.getId(),
            slotType,
            parkingRate.setScale(RATE_SCALE, RoundingMode.HALF_UP),
            slots.size(),
            averageOccupancyRate,
            averageOccupancyMultiplier.setScale(RATE_SCALE, RoundingMode.HALF_UP),
            new DynamicPriceBreakdown(baseAmount, taxAmount, totalAmount, slotRate.currency()));
    }

    private Money resolveSlotRate(PricingRule pricingRule, PricingSlotType slotType) {
        return switch (slotType) {
            case SHORT -> {
                if (pricingRule.getHourlyRate() == null) {
                    throw new IllegalArgumentException("hourlyRate is required for SHORT dynamic pricing");
                }
                yield new Money(
                    pricingRule.getHourlyRate().amount().divide(BigDecimal.valueOf(2), RATE_SCALE, RoundingMode.HALF_UP),
                    pricingRule.getHourlyRate().currency());
            }
            case LONG -> {
                if (pricingRule.getDailyRate() == null) {
                    throw new IllegalArgumentException("dailyRate is required for LONG dynamic pricing");
                }
                yield pricingRule.getDailyRate();
            }
        };
    }
}
