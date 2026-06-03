package org.labcabrera.parking.catalog.application.cqrs.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetFacilityInventoryQuery(
    UUID facilityId,
    LocalDateTime start,
    LocalDateTime end) {
}
