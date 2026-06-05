package org.labcabrera.parking.facilities.domain.valueobject;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Identifies an inventory bucket for a facility. SHORT_TERM buckets use half-hour
 * boundaries. LONG_TERM buckets use day starts.
 */
public record SlotKey(UUID facilityId, LocalDateTime slotStart, InventoryBlockType blockType) {

    public SlotKey(UUID facilityId, LocalDateTime slotStart) {
        this(facilityId, slotStart, InventoryBlockType.SHORT_TERM);
    }
}
