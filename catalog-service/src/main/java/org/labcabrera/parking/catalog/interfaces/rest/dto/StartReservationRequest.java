package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record StartReservationRequest(
    @NotNull UUID facilityId,
    @NotNull LocalDateTime checkIn,
    @NotNull LocalDateTime checkOut) {
}
