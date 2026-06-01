package org.labcabrera.parking.reservation.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-side projection of a SpotHold, used by the domain port HoldRepository.
 * Decouples the domain layer from JPA infrastructure types.
 */
public record HoldReadModel(
        UUID holdId,
        String searchSessionId,
        UUID spotId,
        UUID facilityId,
        String visitorIp,
        Instant checkIn,
        Instant checkOut,
        Money estimatedPrice,
        Money confirmedPrice,
        HoldStatus status,
        Instant expiresAt
) {}
