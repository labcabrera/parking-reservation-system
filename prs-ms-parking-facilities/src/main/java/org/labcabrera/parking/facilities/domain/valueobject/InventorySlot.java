package org.labcabrera.parking.facilities.domain.valueobject;

import java.time.LocalDateTime;

/**
 * Read-only view of an inventory slot.
 */
public record InventorySlot(LocalDateTime slotStart, InventoryBlockType blockType, int capacity, int reserved) {

    public int free() {
        return capacity - reserved;
    }
}
