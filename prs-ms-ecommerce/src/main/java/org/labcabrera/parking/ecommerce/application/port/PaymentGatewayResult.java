package org.labcabrera.parking.ecommerce.application.port;

public record PaymentGatewayResult(
    boolean success,
    String transactionId,
    String failureReason) {

    public static PaymentGatewayResult succeeded(String transactionId) {
        return new PaymentGatewayResult(true, transactionId, null);
    }

    public static PaymentGatewayResult failed(String reason) {
        return new PaymentGatewayResult(false, null, reason);
    }
}
