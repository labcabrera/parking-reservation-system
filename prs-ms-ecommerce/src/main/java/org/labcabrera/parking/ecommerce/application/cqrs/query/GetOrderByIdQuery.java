package org.labcabrera.parking.ecommerce.application.cqrs.query;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record GetOrderByIdQuery(@NotNull UUID orderId) {
}
