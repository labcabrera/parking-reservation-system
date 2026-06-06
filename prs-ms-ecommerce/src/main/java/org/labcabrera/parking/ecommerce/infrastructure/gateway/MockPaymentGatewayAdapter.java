package org.labcabrera.parking.ecommerce.infrastructure.gateway;

import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.PaymentGatewayPort;
import org.labcabrera.parking.ecommerce.application.port.PaymentGatewayResult;
import org.labcabrera.parking.ecommerce.domain.valueobject.Money;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Stub implementation of {@link PaymentGatewayPort}.
 *
 * <p>
 * Active when {@code ecommerce.payment.gateway=mock} (the default). Replace with a real
 * HTTP client adapter pointing at a payment provider.
 *
 * <p>
 * The implementation is idempotent: the same {@code idempotencyKey} will always produce
 * the same synthetic transaction id, mirroring what a real gateway would do with
 * idempotency keys.
 */
@Component
@ConditionalOnProperty(name = "ecommerce.payment.gateway", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public PaymentGatewayResult charge(String idempotencyKey, Money amount, String paymentMethodCode) {
        log.info("Mock gateway: charging {} {} via {} (idempotencyKey={})",
            amount.amount(), amount.currency(), paymentMethodCode, idempotencyKey);

        // Derive a deterministic transaction id from the idempotency key so that
        // replaying the same key always yields the same result (gateway-side idempotency).
        String transactionId = "TXN-" + UUID.nameUUIDFromBytes(idempotencyKey.getBytes());

        log.info("Mock gateway: charge approved — transactionId={}", transactionId);
        return PaymentGatewayResult.succeeded(transactionId);
    }
}
