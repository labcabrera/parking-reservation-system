package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FacilityAvailabilityDto(
    UUID id,
    String name,
    String city,
    String address,
    int totalSpots,
    int availableSpots,
    boolean lowAvailability,
    BigDecimal estimatedPrice,
    String currency) {
}
