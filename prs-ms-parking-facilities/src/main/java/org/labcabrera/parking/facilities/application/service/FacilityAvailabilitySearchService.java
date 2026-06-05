package org.labcabrera.parking.facilities.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.labcabrera.parking.facilities.domain.port.FacilityAvailabilityReadModel;
import org.labcabrera.parking.facilities.domain.port.InventoryRepository;
import org.labcabrera.parking.facilities.domain.port.FacilityAvailabilityReadModel.FacilityAvailabilityRow;
import org.labcabrera.parking.facilities.domain.service.SlotCalculator;
import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockPlan;
import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockType;
import org.labcabrera.parking.facilities.domain.valueobject.InventorySlot;
import org.labcabrera.parking.facilities.interfaces.rest.dto.FacilityAvailabilityDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-only text + dates availability search. Resolves up to N active facilities matching
 * the text against name/city/address, then computes peak slot occupancy across the
 * requested interval. Pure read path — no locking, no Axon, safe under high concurrency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityAvailabilitySearchService {

    private final FacilityAvailabilityReadModel readModel;
    private final InventoryRepository inventoryRepository;

    @Value("${catalog.search.default-limit:20}")
    private int defaultLimit;

    @Value("${catalog.search.max-page-size:100}")
    private int maxLimit;

    @Value("${catalog.search.low-availability-threshold:5}")
    private int lowAvailabilityThreshold;

    @Value("${catalog.reservation.short-duration-threshold-hours:12}")
    private long shortDurationThresholdHours;

    public List<FacilityAvailabilityDto> search(String text, LocalDateTime checkIn, LocalDateTime checkOut,
        Integer requestedLimit) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        int limit = Math.min(requestedLimit != null && requestedLimit > 0 ? requestedLimit : defaultLimit, maxLimit);
        InventoryBlockPlan plan = SlotCalculator.planFor(
            null,
            checkIn,
            checkOut,
            Duration.ofHours(shortDurationThresholdHours));
        LocalDateTime gridStart = plan.slots().get(0).slotStart();
        LocalDateTime gridEnd = plan.blockType() == InventoryBlockType.LONG_TERM
            ? plan.slots().get(plan.slots().size() - 1).slotStart().plusDays(1)
            : SlotCalculator.ceilToSlot(checkOut);

        List<FacilityAvailabilityRow> rows = readModel.findCandidates(text, gridStart, gridEnd, limit, plan.blockType());
        long minutes = Duration.between(checkIn, checkOut).toMinutes();
        BigDecimal minutesValue = BigDecimal.valueOf(minutes);
        BigDecimal minutesPerDay = BigDecimal.valueOf(24L * 60L);

        List<FacilityAvailabilityDto> results = new ArrayList<>(rows.size());
        for (FacilityAvailabilityRow row : rows) {
            int capacity = row.capacity().capacityFor(plan.blockType());
            int available = Math.max(0, capacity - row.maxReserved());
            if (available <= 0) {
                continue;
            }
            BigDecimal price = row.dailyPrice()
                .multiply(minutesValue)
                .divide(minutesPerDay, 2, RoundingMode.HALF_UP);
            results.add(new FacilityAvailabilityDto(
                row.id(),
                row.name(),
                row.city(),
                row.address(),
                row.capacity(),
                plan.blockType(),
                available,
                available <= lowAvailabilityThreshold,
                price,
                "EUR"));
        }
        log.debug("Availability search text='{}' [{}, {}) -> {} candidates, {} with availability",
            text, gridStart, gridEnd, rows.size(), results.size());
        return results;
    }

    public List<InventorySlot> getInventorySlots(java.util.UUID facilityId, LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
        InventoryBlockPlan plan = SlotCalculator.planFor(
            facilityId,
            start,
            end,
            Duration.ofHours(shortDurationThresholdHours));
        LocalDateTime gridStart = plan.slots().get(0).slotStart();
        LocalDateTime gridEnd = plan.blockType() == InventoryBlockType.LONG_TERM
            ? plan.slots().get(plan.slots().size() - 1).slotStart().plusDays(1)
            : SlotCalculator.ceilToSlot(end);
        return inventoryRepository.findSlots(facilityId, gridStart, gridEnd, plan.blockType());
    }
}
