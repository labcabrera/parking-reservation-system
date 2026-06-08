package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCallbackRequest(
    @NotNull UUID orderId,
    @NotNull UUID paymentAttemptId,
    @NotBlank String status,
    String gatewayTransactionId,
    String failureReason) {
}
