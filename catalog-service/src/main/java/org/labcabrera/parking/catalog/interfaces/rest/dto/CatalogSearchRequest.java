package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record CatalogSearchRequest(
    String query,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    Set<String> features
) {
}
