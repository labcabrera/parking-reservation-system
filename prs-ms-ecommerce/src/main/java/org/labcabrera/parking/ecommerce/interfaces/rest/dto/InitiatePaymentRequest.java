package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InitiatePaymentRequest(

    /**
     * Client-supplied idempotency key for this payment attempt. Re-submitting the same
     * key is safe: the service will return the result of the original attempt without
     * charging the customer again. Use a fresh UUID for each new retry after a failure.
     */
    @NotNull UUID idempotencyKey,

    @NotBlank String paymentMethodCode) {
}
