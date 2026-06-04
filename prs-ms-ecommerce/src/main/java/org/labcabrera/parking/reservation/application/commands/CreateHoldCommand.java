package org.labcabrera.parking.reservation.application.commands;

import org.axonframework.commandhandling.annotations.RoutingKey;
import org.labcabrera.parking.reservation.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public record CreateHoldCommand(
        @RoutingKey UUID holdId,
        String searchSessionId,
        UUID spotId,
        UUID facilityId,
        String visitorIp,
        Instant checkIn,
        Instant checkOut,
        Money estimatedPrice,
        Instant expiresAt) {
}
