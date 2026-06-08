package org.labcabrera.parking.facilities.application.cqrs.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ExpireReservationPaymentCommand(

    @TargetAggregateIdentifier UUID reservationId,
    UUID orderId) {
}
