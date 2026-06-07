package org.labcabrera.parking.ecommerce.application.cqrs.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderCommand(

    @TargetAggregateIdentifier
    @NotNull
    UUID orderId,

    @NotNull
    UUID holdId,

    String bookingSessionId,

    @NotNull
    @Future
    LocalDateTime expiresAt,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal amount,

    @NotBlank
    String currency) {
}
