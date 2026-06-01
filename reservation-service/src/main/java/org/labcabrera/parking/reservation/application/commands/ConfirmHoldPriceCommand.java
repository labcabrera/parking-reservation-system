package org.labcabrera.parking.reservation.application.commands;

import org.axonframework.commandhandling.annotations.RoutingKey;
import org.labcabrera.parking.reservation.domain.model.Money;

import java.util.UUID;

public record ConfirmHoldPriceCommand(
        @RoutingKey UUID holdId,
        Money confirmedPrice) {
}
