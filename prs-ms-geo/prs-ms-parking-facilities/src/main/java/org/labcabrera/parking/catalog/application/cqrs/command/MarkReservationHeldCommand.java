package org.labcabrera.parking.catalog.application.cqrs.command;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record MarkReservationHeldCommand(
    @TargetAggregateIdentifier UUID reservationId,
    BigDecimal estimatedPrice,
    String currency) {
}
