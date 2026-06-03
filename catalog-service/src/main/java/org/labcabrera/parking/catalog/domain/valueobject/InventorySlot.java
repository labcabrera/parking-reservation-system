package org.labcabrera.parking.catalog.domain.valueobject;

import java.time.LocalDateTime;

/**
 * Read-only view of an inventory slot.
 */
public record InventorySlot(LocalDateTime slotStart, int capacity, int reserved) {

    public int free() {
        return capacity - reserved;
    }
}
