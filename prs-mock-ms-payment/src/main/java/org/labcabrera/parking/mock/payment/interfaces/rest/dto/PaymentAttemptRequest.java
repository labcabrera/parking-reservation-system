package org.labcabrera.parking.mock.payment.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAttemptRequest(

    UUID paymentAttemptId,

    @NotNull UUID orderId,

    String idempotencyKey,

    String paymentMethodCode,

    @NotNull @Positive BigDecimal amount,

    @NotBlank String currency,

    String ecommerceCallbackUrl,

    @NotBlank String callbackUrl) {
}
