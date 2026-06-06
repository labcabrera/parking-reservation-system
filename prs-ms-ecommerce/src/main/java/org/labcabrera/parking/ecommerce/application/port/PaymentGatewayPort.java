package org.labcabrera.parking.ecommerce.application.port;

import org.labcabrera.parking.ecommerce.domain.valueobject.Money;

public interface PaymentGatewayPort {

    /**
     * Charges the given amount using the specified payment method.
     *
     * <p>
     * Implementations MUST be idempotent: calling this method twice with the same
     * {@code idempotencyKey} must not result in a double charge.
     *
     * @param idempotencyKey stable key uniquely identifying this charge attempt
     * @param amount amount and currency to charge
     * @param paymentMethodCode code of the payment method (e.g. "CREDIT_CARD")
     * @return result indicating success or failure
     */
    PaymentGatewayResult charge(String idempotencyKey, Money amount, String paymentMethodCode);
}
