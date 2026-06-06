package org.labcabrera.parking.bff.interfaces.rest.dto;

import java.util.UUID;

/**
 * Result of a payment attempt. When {@code status} is {@code SUCCESS}, the user must be
 * redirected to {@code redirectUrl}.
 */
public record PaymentAttemptResultDto(

    UUID attemptId,

    /** Payment attempt outcome: SUCCESS, FAILED, PENDING. */
    String status,

    /** URL to redirect the user to the payment gateway page. */
    String redirectUrl) {
}
