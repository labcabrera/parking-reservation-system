package org.labcabrera.parking.reservation.interfaces.rest;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request body for creating a parking spot hold.
 */
public record CreateHoldRequest(
        @NotNull String searchSessionId,
        @NotNull UUID spotId,
        @NotNull UUID facilityId,
        @NotNull Instant checkIn,
        @NotNull Instant checkOut,
        @NotNull BigDecimal estimatedPriceAmount,
        @NotNull String currency) {
}
