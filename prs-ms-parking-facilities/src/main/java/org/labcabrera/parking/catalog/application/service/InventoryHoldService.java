package org.labcabrera.parking.catalog.application.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.port.InventoryRepository;
import org.labcabrera.parking.catalog.domain.port.ParkingFacilityRepository;
import org.labcabrera.parking.catalog.domain.service.SlotCalculator;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobject.InventoryBlockPlan;
import org.labcabrera.parking.catalog.domain.valueobject.SlotKey;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${catalog.reservation.short-duration-threshold-hours:12}")
    private long shortDurationThresholdHours;

    public HoldResult tryHold(UUID facilityId, LocalDateTime checkIn, LocalDateTime checkOut) {
        log.info("Attempting inventory hold for facility {}, checkIn {}, checkOut {}", facilityId, checkIn, checkOut);

        var facilityOpt = facilityRepository.findById(new FacilityId(facilityId));
        if (facilityOpt.isEmpty()) {
            log.warn("Facility {} not found", facilityId);
            return HoldResult.failure("Facility not found: " + facilityId);
        }
        InventoryBlockPlan plan = SlotCalculator.planFor(
            facilityId,
            checkIn,
            checkOut,
            Duration.ofHours(shortDurationThresholdHours));
        int capacity = facilityOpt.get().getCapacity().capacityFor(plan.blockType());
        if (capacity < 1) {
            return HoldResult.failure("No capacity configured for " + plan.blockType() + " reservations");
        }

        List<SlotKey> slots = plan.slots();
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

    public void release(UUID facilityId, LocalDateTime checkIn, LocalDateTime checkOut) {
        log.info("Releasing inventory hold for facility {}, checkIn {}, checkOut {}", facilityId, checkIn, checkOut);
        List<SlotKey> slots = SlotCalculator.planFor(
            facilityId,
            checkIn,
            checkOut,
            Duration.ofHours(shortDurationThresholdHours)).slots();
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
