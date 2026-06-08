package org.labcabrera.parking.mock.payment.interfaces.rest.dto;

public record PaymentCaptureRequest(
    String status,
    String callbackUrl) {
}
