package org.labcabrera.parking.reservation.domain.model.events;

import java.time.Instant;
import java.util.UUID;

public record HoldConvertedEvent(
        UUID holdId,
        UUID reservationId,
        Instant occurredAt) {
}
