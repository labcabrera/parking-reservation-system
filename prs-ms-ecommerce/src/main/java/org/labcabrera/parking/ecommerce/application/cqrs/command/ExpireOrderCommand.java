package org.labcabrera.parking.ecommerce.application.cqrs.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import jakarta.validation.constraints.NotNull;

public record ExpireOrderCommand(

    @TargetAggregateIdentifier
    @NotNull
    UUID orderId) {
}
