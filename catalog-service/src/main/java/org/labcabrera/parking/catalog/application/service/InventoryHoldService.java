package org.labcabrera.parking.catalog.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.port.outbound.InventoryRepository;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
import org.labcabrera.parking.catalog.domain.service.SlotCalculator;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobjects.SlotKey;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the multi-slot hold with bounded retries on optimistic-lock
 * collisions and full compensation (release every previously held slot) when
 * any single slot cannot be reserved.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryHoldService {

    private static final int MAX_RETRIES_PER_SLOT = 5;

    private final InventoryRepository inventory;
    private final ParkingFacilityRepository facilityRepository;

    public HoldResult tryHold(UUID facilityId, java.time.LocalDateTime checkIn, java.time.LocalDateTime checkOut) {
        int capacity = facilityRepository.findById(new FacilityId(facilityId))
            .map(f -> f.getTotalSpots())
            .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + facilityId));

        List<SlotKey> slots = SlotCalculator.slotsFor(facilityId, checkIn, checkOut);
        List<SlotKey> held = new ArrayList<>(slots.size());

        for (SlotKey slot : slots) {
            boolean ok = false;
            for (int attempt = 0; attempt < MAX_RETRIES_PER_SLOT && !ok; attempt++) {
                ok = inventory.tryHold(slot, capacity);
            }
            if (!ok) {
                log.info("Hold failed on slot {} after retries; compensating {} previously held slots",
                    slot, held.size());
                inventory.release(held, facilityId);
                return HoldResult.failure("No capacity on slot " + slot.slotStart());
            }
            held.add(slot);
        }
        return HoldResult.success(held);
    }

    public void release(UUID facilityId, java.time.LocalDateTime checkIn, java.time.LocalDateTime checkOut) {
        List<SlotKey> slots = SlotCalculator.slotsFor(facilityId, checkIn, checkOut);
        inventory.release(slots, facilityId);
    }

    public record HoldResult(boolean success, List<SlotKey> heldSlots, String failureReason) {

        public static HoldResult success(List<SlotKey> slots) {
            return new HoldResult(true, slots, null);
        }

        public static HoldResult failure(String reason) {
            return new HoldResult(false, List.of(), reason);
        }
    }
}
