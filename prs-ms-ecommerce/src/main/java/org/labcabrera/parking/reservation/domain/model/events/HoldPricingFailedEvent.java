package org.labcabrera.parking.reservation.domain.model.events;

import java.time.Instant;
import java.util.UUID;

public record HoldPricingFailedEvent(
        UUID holdId,
        String reason,
        Instant occurredAt) {
}
