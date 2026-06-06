package org.labcabrera.parking.ecommerce.domain.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.exception.DomainException;
import org.labcabrera.parking.ecommerce.domain.valueobject.Money;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentAttemptStatus;

import lombok.Getter;

/**
 * Domain aggregate that represents a single attempt to charge a payment for an order.
 *
 * <p>
 * Idempotency is enforced at two levels:
 * <ol>
 * <li>The {@code idempotencyKey} is stored with a DB unique constraint so that two
 * concurrent inserts for the same key yield only one record.</li>
 * <li>The aggregate state machine prevents re-processing an attempt that is already
 * PROCESSING, SUCCEEDED, or FAILED.</li>
 * </ol>
 *
 * <p>
 * The {@code idempotencyKey} is forwarded to the payment gateway so that, even if the
 * gateway is called twice (e.g. due to a network timeout retry), the gateway can
 * deduplicate the charge on its side.
 */
@Getter
public class PaymentAttempt {

    private UUID id;

    private UUID orderId;

    /**
     * Stable key forwarded verbatim to the payment gateway. Must be unique per attempt.
     */
    private String idempotencyKey;

    private String paymentMethodCode;

    private Money amount;

    private PaymentAttemptStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private String gatewayTransactionId;

    private String failureReason;

    private Long version;

    public PaymentAttempt(
        UUID id,
        UUID orderId,
        String idempotencyKey,
        String paymentMethodCode,
        Money amount,
        PaymentAttemptStatus status,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        String gatewayTransactionId,
        String failureReason,
        Long version) {
        this.id = id;
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.paymentMethodCode = paymentMethodCode;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.gatewayTransactionId = gatewayTransactionId;
        this.failureReason = failureReason;
        this.version = version;
    }

    public static PaymentAttempt create(
        UUID orderId,
        String idempotencyKey,
        String paymentMethodCode,
        Money amount) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        return new PaymentAttempt(
            UUID.randomUUID(),
            orderId,
            idempotencyKey,
            paymentMethodCode,
            amount,
            PaymentAttemptStatus.PENDING,
            LocalDateTime.now(),
            null, null, null, null);
    }

    /**
     * Transitions from PENDING → PROCESSING. Called just before invoking the gateway.
     */
    public void markProcessing() {
        if (this.status != PaymentAttemptStatus.PENDING) {
            throw new DomainException(
                "Cannot start processing a payment attempt in status " + status);
        }
        this.status = PaymentAttemptStatus.PROCESSING;
    }

    /**
     * Transitions from PROCESSING → SUCCEEDED. Called after a successful gateway
     * response.
     */
    public void markSucceeded(String gatewayTransactionId) {
        if (this.status != PaymentAttemptStatus.PROCESSING) {
            throw new DomainException(
                "Cannot mark succeeded a payment attempt in status " + status);
        }
        this.status = PaymentAttemptStatus.SUCCEEDED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Transitions to FAILED. Idempotent: if already terminal, does nothing.
     */
    public void markFailed(String reason) {
        if (this.status.isTerminal()) {
            return;
        }
        this.status = PaymentAttemptStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }
}
