package org.labcabrera.parking.ecommerce.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentAttempt;

public interface PaymentAttemptRepository {

    PaymentAttempt save(PaymentAttempt attempt);

    Optional<PaymentAttempt> findById(UUID id);

    /**
     * Looks up an attempt by its idempotency key. Used to detect and short-circuit
     * duplicate requests.
     */
    Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey);

    List<PaymentAttempt> findByOrderId(UUID orderId);
}
