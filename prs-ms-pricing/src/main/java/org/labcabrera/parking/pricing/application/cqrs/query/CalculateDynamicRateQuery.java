package org.labcabrera.parking.pricing.application.cqrs.query;

import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;
import org.labcabrera.parking.pricing.domain.valueobject.SlotOccupancy;

public record CalculateDynamicRateQuery(
    UUID pricingRuleId,
    PricingSlotType slotType,
    List<SlotOccupancy> slots) {
}
