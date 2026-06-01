package org.labcabrera.parking.reservation.domain.model.events;

import org.labcabrera.parking.reservation.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public record HoldCreatedEvent(
        UUID holdId,
        String searchSessionId,
        UUID spotId,
        UUID facilityId,
        String visitorIp,
        Instant checkIn,
        Instant checkOut,
        Money estimatedPrice,
        Instant expiresAt,
        Instant occurredAt) {
}
