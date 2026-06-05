package org.labcabrera.parking.facilities.application.cqrs.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record MarkReservationFailedCommand(

    @TargetAggregateIdentifier UUID reservationId,

    String reason) {
}
