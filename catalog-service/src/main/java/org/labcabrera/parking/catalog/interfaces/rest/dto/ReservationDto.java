package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "Reservation", description = "Current status and summary for a reservation")
public record ReservationDto(

    @Schema(description = "Reservation id", examples = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = RequiredMode.REQUIRED)
    UUID id,

    @Schema(description = "Facility id", examples = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = RequiredMode.REQUIRED)
    UUID facilityId,

    @Schema(description = "User id who started the reservation", requiredMode = RequiredMode.REQUIRED)
    String userId,

    @Schema(description = "Check-in datetime", examples="2026-01-01T15:00:00.000Z", requiredMode = RequiredMode.REQUIRED)
    LocalDateTime checkIn,

    @Schema(description = "Check-out datetime", examples="2026-01-02T15:00:00.000Z", requiredMode = RequiredMode.REQUIRED)
    LocalDateTime checkOut,

    @Schema(description = "Reservation lifecycle status", examples = "HELD")
    String status,

    @Schema(description = "Estimated price for the reservation interval")
    BigDecimal estimatedPrice,

    @Schema(description = "Currency code", examples = "EUR")
    String currency,

    @Schema(description = "Hold expiry datetime", examples="2026-01-02T15:43:32.123Z")
    LocalDateTime expiresAt,

    @Schema(description = "Failure reason, when status is FAILED or similar")
    String failureReason) {
}
