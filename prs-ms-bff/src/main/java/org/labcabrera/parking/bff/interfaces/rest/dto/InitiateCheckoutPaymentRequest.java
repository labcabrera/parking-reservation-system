package org.labcabrera.parking.bff.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Request to initiate a payment attempt for a confirmed checkout.
 */
public record InitiateCheckoutPaymentRequest(

    /** Code identifying the payment method to use (e.g. "VISA", "PAYPAL"). */
    @NotBlank String paymentMethodCode,

    /**
     * Client-supplied idempotency key. The caller MUST generate this key on the first
     * attempt and reuse the same value on every retry for the same payment intent. If
     * null the BFF generates a new key, which is only safe for a first attempt.
     */
    UUID idempotencyKey) {
}
