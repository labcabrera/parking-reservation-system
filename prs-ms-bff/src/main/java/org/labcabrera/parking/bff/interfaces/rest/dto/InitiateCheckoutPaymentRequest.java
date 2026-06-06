package org.labcabrera.parking.bff.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to initiate a payment attempt for a confirmed checkout.
 */
public record InitiateCheckoutPaymentRequest(

    /** Code identifying the payment method to use (e.g. "VISA", "PAYPAL"). */
    @NotBlank String paymentMethodCode) {
}
