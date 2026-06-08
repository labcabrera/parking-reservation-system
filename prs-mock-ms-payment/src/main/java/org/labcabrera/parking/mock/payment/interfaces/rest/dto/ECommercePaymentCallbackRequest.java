package org.labcabrera.parking.mock.payment.interfaces.rest.dto;

import java.util.UUID;

public record ECommercePaymentCallbackRequest(
    UUID orderId,
    UUID paymentAttemptId,
    String status,
    String gatewayTransactionId,
    String failureReason) {
}
