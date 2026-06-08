package org.labcabrera.parking.pricing.application.event;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;

public record UpdatedParkingFacilityCapacityEvent(
    UUID parkingFacilityId,
    PricingSlotType slotType,
    List<SlotCapacity> slots) {

    public record SlotCapacity(
        Duration slotDuration,
        BigDecimal capacityPercentage) {
    }
}
