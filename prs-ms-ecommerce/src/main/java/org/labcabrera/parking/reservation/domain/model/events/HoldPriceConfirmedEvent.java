package org.labcabrera.parking.reservation.domain.model.events;

import org.labcabrera.parking.reservation.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public record HoldPriceConfirmedEvent(
        UUID holdId,
        Money confirmedPrice,
        Instant occurredAt) {
}
