package org.labcabrera.parking.catalog.domain.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Read-only port for the text + dates availability search. Returns a denormalized
 * view per candidate facility, joining facility metadata with the peak inventory
 * occupancy across the requested half-hour grid in a single round-trip.
 */
public interface FacilityAvailabilityReadModel {

    List<FacilityAvailabilityRow> findCandidates(String text, LocalDateTime gridStart, LocalDateTime gridEnd,
        int limit);

    record FacilityAvailabilityRow(
        UUID id,
        String name,
        String city,
        String address,
        int totalSpots,
        int maxReserved,
        BigDecimal dailyPrice) {
    }
}
