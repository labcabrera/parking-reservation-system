package org.labcabrera.parking.catalog.application.cqrs.command;

import java.time.LocalDateTime;

public record StartReservationCommand(
    String facilityId,
    String queryText,
    LocalDateTime checkIn,
    LocalDateTime checkOut) {

    public StartReservationCommand {
        boolean hasFacilityId = facilityId != null && !facilityId.isBlank();
        boolean hasQueryText = queryText != null && !queryText.isBlank();
        if (hasFacilityId == hasQueryText) {
            throw new IllegalArgumentException("Exactly one of 'facilityId' or 'queryText' must be provided");
        }
    }

}
