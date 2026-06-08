package org.labcabrera.parking.ecommerce.infrastructure.gateway;

import java.math.BigDecimal;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.PaymentGatewayPort;
import org.labcabrera.parking.ecommerce.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    private final RestClient restClient;
    private final String customerCallbackUrl;
    private final String ecommerceCallbackUrl;

    public MockPaymentGatewayAdapter(
        RestClient.Builder builder,
        @Value("${ecommerce.payment.mock.base-url:http://localhost:8089}") String baseUrl,
        @Value("${ecommerce.payment.mock.customer-callback-url:http://localhost:3001/payment-result}") String customerCallbackUrl,
        @Value("${ecommerce.payment.mock.ecommerce-callback-url:http://localhost:8082/api/v1/payment-callbacks/mock}") String ecommerceCallbackUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.customerCallbackUrl = customerCallbackUrl;
        this.ecommerceCallbackUrl = ecommerceCallbackUrl;
    }

    @Override
    public void charge(UUID orderId, UUID paymentAttemptId, String idempotencyKey, Money amount, String paymentMethodCode) {
        log.info("Mock gateway: registering payment attempt {} for order {} via {}", paymentAttemptId, orderId, paymentMethodCode);

        restClient.post()
            .uri("/api/v1/payment-attempts")
            .body(new MockPaymentAttemptRequest(
                paymentAttemptId,
                orderId,
                idempotencyKey,
                paymentMethodCode,
                amount.amount(),
                amount.currency(),
                ecommerceCallbackUrl,
                customerCallbackUrl))
            .retrieve()
            .toBodilessEntity();
    }

    private record MockPaymentAttemptRequest(
        UUID paymentAttemptId,
        UUID orderId,
        String idempotencyKey,
        String paymentMethodCode,
        BigDecimal amount,
        String currency,
        String ecommerceCallbackUrl,
        String callbackUrl) {
    }
}
