package org.labcabrera.parking.catalog.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.labcabrera.parking.catalog.domain.port.FacilityAvailabilityReadModel;
import org.labcabrera.parking.catalog.domain.port.InventoryRepository;
import org.labcabrera.parking.catalog.domain.valueobject.InventorySlot;
import org.labcabrera.parking.catalog.domain.port.FacilityAvailabilityReadModel.FacilityAvailabilityRow;
import org.labcabrera.parking.catalog.domain.service.SlotCalculator;
import org.labcabrera.parking.catalog.interfaces.rest.dto.FacilityAvailabilityDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-only text + dates availability search. Resolves up to N active
 * facilities matching the text against name/city/address, then computes peak
 * slot occupancy across the requested interval. Pure read path — no locking,
 * no Axon, safe under high concurrency.
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

    public List<FacilityAvailabilityDto> search(String text, LocalDateTime checkIn, LocalDateTime checkOut,
            Integer requestedLimit) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        int limit = Math.min(requestedLimit != null && requestedLimit > 0 ? requestedLimit : defaultLimit, maxLimit);
        LocalDateTime gridStart = SlotCalculator.floorToSlot(checkIn);
        LocalDateTime gridEnd = SlotCalculator.ceilToSlot(checkOut);

        List<FacilityAvailabilityRow> rows = readModel.findCandidates(text, gridStart, gridEnd, limit);
        long minutes = Duration.between(checkIn, checkOut).toMinutes();
        BigDecimal minutesValue = BigDecimal.valueOf(minutes);
        BigDecimal minutesPerDay = BigDecimal.valueOf(24L * 60L);

        List<FacilityAvailabilityDto> results = new ArrayList<>(rows.size());
        for (FacilityAvailabilityRow row : rows) {
            int available = Math.max(0, row.totalSpots() - row.maxReserved());
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
                row.totalSpots(),
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
        return inventoryRepository.findSlots(facilityId, start, end);
    }
}
