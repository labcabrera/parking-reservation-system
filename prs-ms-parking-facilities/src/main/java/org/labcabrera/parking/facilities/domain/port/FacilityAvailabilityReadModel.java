package org.labcabrera.parking.facilities.domain.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockType;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;

/**
 * Read-only port for the text + dates availability search. Returns a denormalized view
 * per candidate facility, joining facility metadata with the peak inventory occupancy
 * across the requested half-hour grid in a single round-trip.
 */
public interface FacilityAvailabilityReadModel {

    List<FacilityAvailabilityRow> findCandidates(String text, LocalDateTime gridStart, LocalDateTime gridEnd,
        int limit, InventoryBlockType blockType);

    record FacilityAvailabilityRow(
        UUID id,
        String name,
        String city,
        String address,
        ParkingCapacity capacity,
        int maxReserved,
        BigDecimal dailyPrice) {
    }
}
