package org.labcabrera.parking.reservation.application.commands;

import org.axonframework.commandhandling.annotations.RoutingKey;

import java.util.UUID;

public record FailHoldPricingCommand(
        @RoutingKey UUID holdId,
        String reason) {
}
