package org.labcabrera.parking.facilities.application.port;

import java.math.BigDecimal;
import java.util.UUID;

public record DynamicPrice(
    UUID pricingRuleId,
    DynamicPricingSlotType slotType,
    int slotCount,
    BigDecimal averageOccupancyRate,
    BigDecimal averageOccupancyMultiplier,
    BigDecimal baseAmount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String currency) {
}
