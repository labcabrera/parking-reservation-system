package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DynamicRateResponse", description = "Calculated dynamic price and the intermediate pricing factors")
public record DynamicRateResponse(
    @Schema(description = "Pricing rule identifier used for the calculation", example = "018f4f3b-9c52-7d87-9f0b-7b2d8a4b8c12")
    UUID pricingRuleId,
    @Schema(description = "Slot duration model used in the calculation", example = "SHORT")
    PricingSlotType slotType,
    @Schema(description = "Number of slots used in the calculation", example = "2")
    int slotCount,
    @Schema(description = "Average occupancy rate of all involved slots", example = "0.7500")
    BigDecimal averageOccupancyRate,
    @Schema(description = "Average occupancy multiplier applied to the base slot total", example = "1.7500")
    BigDecimal averageOccupancyMultiplier,
    @Schema(description = "Price breakdown with base amount, taxes, total amount, and currency")
    DynamicPriceBreakdownDto price) {
}
