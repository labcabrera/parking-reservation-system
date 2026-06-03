package org.labcabrera.parking.catalog.application.cqrs.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ConfirmReservationCommand(
    
    @TargetAggregateIdentifier
    UUID reservationId) {
}
