package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;


public record CatalogSearchRequest(
    UUID parkingFacilityId,

    String query,

    LocalDateTime checkIn,

    LocalDateTime checkOut
) {
}

