package org.labcabrera.parking.reservation.domain.model.events;

import java.time.Instant;
import java.util.UUID;

public record HoldReleasedEvent(
        UUID holdId,
        UUID facilityId,
        UUID spotId,
        Instant checkIn,
        Instant checkOut,
        Instant occurredAt) {
}
