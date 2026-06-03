package org.labcabrera.parking.catalog.domain.port;

import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.valueobject.SlotKey;

/**
 * Outbound port for the half-hour inventory grid. Implementations must guarantee
 * per-slot atomicity (no oversell) using optimistic locking.
 */
public interface InventoryRepository {

    /**
     * Attempts to atomically increment {@code reserved} on the given slot up to
     * {@code capacity}. Returns true on success. If the slot does not exist yet,
     * it is created using {@code defaultCapacity}.
     */
    boolean tryHold(SlotKey slot, int defaultCapacity);

    /**
     * Releases one unit on each given slot. Idempotent and best-effort.
     */
    void release(List<SlotKey> slots, UUID facilityId);

    /**
     * Returns the inventory slots for a facility within [start, end).
     */
    List<org.labcabrera.parking.catalog.domain.valueobject.InventorySlot> findSlots(UUID facilityId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
