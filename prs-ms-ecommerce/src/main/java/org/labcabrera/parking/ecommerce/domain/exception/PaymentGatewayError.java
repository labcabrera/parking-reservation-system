package org.labcabrera.parking.ecommerce.domain.exception;

public class PaymentGatewayError extends DomainException {

    public PaymentGatewayError(String message, Throwable cause) {
        super(message, cause);
    }

}
