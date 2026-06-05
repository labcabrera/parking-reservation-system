package org.labcabrera.parking.facilities.application.cqrs.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CancelReservationCommand(

    @TargetAggregateIdentifier UUID reservationId,

    String reason) {
}
