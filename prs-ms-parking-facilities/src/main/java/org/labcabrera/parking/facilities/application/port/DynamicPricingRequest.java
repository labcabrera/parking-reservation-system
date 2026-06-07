package org.labcabrera.parking.facilities.application.port;

import java.util.List;
import java.util.UUID;

public record DynamicPricingRequest(
    UUID pricingRuleId,
    DynamicPricingSlotType slotType,
    List<SlotOccupancy> slots) {
}
