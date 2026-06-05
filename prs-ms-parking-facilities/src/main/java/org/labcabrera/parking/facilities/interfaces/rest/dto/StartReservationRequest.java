package org.labcabrera.parking.facilities.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "StartReservationRequest", description = "Request payload to start a reservation. Validation: facilityId, checkIn and checkOut required, checkIn must be before checkOut, checkIn and checkOut must be in the future.")
public record StartReservationRequest(

    @NotNull @Schema(description = "ID of the parking facility to reserve", examples = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) UUID facilityId,

    @NotNull @Schema(description = "Check-in datetime for the reservation", examples = "2026-01-01T15:00:00.000Z", requiredMode = Schema.RequiredMode.REQUIRED) LocalDateTime checkIn,

    @NotNull @Schema(description = "Check-out datetime for the reservation", examples = "2026-01-02T15:00:00.000Z", requiredMode = Schema.RequiredMode.REQUIRED) LocalDateTime checkOut) {
}
