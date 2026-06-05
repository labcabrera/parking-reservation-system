package org.labcabrera.parking.facilities.application.cqrs.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetFacilityInventoryQuery(
    UUID facilityId,
    LocalDateTime start,
    LocalDateTime end) {

    public GetFacilityInventoryQuery {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }
}
