package org.labcabrera.parking.pricing.domain.valueobject;

import java.math.BigDecimal;
import java.util.UUID;

public record DynamicPricingResult(
    UUID pricingRuleId,
    PricingSlotType slotType,
    BigDecimal parkingRate,
    int slotCount,
    BigDecimal averageOccupancyRate,
    BigDecimal averageOccupancyMultiplier,
    DynamicPriceBreakdown price) {
}
