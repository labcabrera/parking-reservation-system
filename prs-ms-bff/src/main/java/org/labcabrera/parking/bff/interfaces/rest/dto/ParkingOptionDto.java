package org.labcabrera.parking.bff.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents an available parking facility returned by the search. The {@code facilityId}
 * is used as input for the select-option step.
 */
public record ParkingOptionDto(

    UUID facilityId,

    String name,

    String city,

    String address,

    Integer availableSpots,

    boolean lowAvailability,

    BigDecimal estimatedPrice,

    String currency) {
}
