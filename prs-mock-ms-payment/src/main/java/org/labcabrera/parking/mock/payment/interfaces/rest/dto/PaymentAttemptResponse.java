package org.labcabrera.parking.mock.payment.interfaces.rest.dto;

import java.util.UUID;

public record PaymentAttemptResponse(

    UUID attemptId,

    String status,

    String redirectUrl) {
}
