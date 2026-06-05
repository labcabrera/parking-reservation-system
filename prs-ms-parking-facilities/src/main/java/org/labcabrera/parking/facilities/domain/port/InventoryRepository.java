package org.labcabrera.parking.facilities.domain.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockType;
import org.labcabrera.parking.facilities.domain.valueobject.InventorySlot;
import org.labcabrera.parking.facilities.domain.valueobject.SlotKey;

/**
 * Outbound port for the half-hour inventory grid. Implementations must guarantee per-slot
 * atomicity (no oversell) using optimistic locking.
 */
public interface InventoryRepository {

    /**
     * Attempts to atomically increment {@code reserved} on the given slot up to
     * {@code capacity}. Returns true on success. If the slot does not exist yet, it is
     * created using {@code defaultCapacity}.
     */
    boolean tryHold(SlotKey slot, int defaultCapacity);

    /**
     * Releases one unit on each given slot. Idempotent and best-effort.
     */
    void release(List<SlotKey> slots, UUID facilityId);

    /**
     * Returns the inventory slots for a facility within [start, end).
     */
    List<InventorySlot> findSlots(UUID facilityId, LocalDateTime start, LocalDateTime end);

    List<InventorySlot> findSlots(UUID facilityId, LocalDateTime start, LocalDateTime end, InventoryBlockType blockType);
}
