package org.labcabrera.parking.ecommerce.application.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.ecommerce.application.cqrs.command.ExpireOrderCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.MarkOrderPaidCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.MarkOrderPaymentFailedCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.MarkOrderPaymentInProgressCommand;
import org.labcabrera.parking.ecommerce.application.port.PaymentAttemptRepository;
import org.labcabrera.parking.ecommerce.application.port.PaymentGatewayPort;
import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentAttempt;
import org.labcabrera.parking.ecommerce.domain.exception.DomainException;
import org.labcabrera.parking.ecommerce.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.ecommerce.domain.exception.InvalidOrderStateException;
import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the payment flow for an order.
 *
 * <h3>Idempotency guarantees</h3>
 * <ol>
 * <li><b>Application-level</b>: before creating a {@link PaymentAttempt} the service
 * looks up the {@code idempotencyKey} in the database and short-circuits if one already
 * exists.</li>
 * <li><b>Database-level</b>: the {@code payment_attempts.idempotency_key} column has a
 * {@code UNIQUE} constraint, so even two concurrent requests with the same key will only
 * produce one row — the second insert will throw a constraint violation.</li>
 * <li><b>Gateway-level</b>: the same {@code idempotencyKey} is forwarded verbatim to the
 * payment gateway, which uses it to prevent double charges on its side.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentService {

    private final CommandGateway commandGateway;
    private final OrderReadRepository orderReadRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentGatewayPort paymentGatewayPort;

    /**
     * Initiates a payment attempt for the given order.
     *
     * <p>
     * The caller is responsible for supplying a stable {@code idempotencyKey} (e.g. a
     * client-generated UUID). Re-submitting the same key is safe and will return the
     * result of the original attempt without charging the customer again.
     *
     * @param orderId the order to pay
     * @param idempotencyKey unique key for this payment attempt
     * @param paymentMethodCode payment method selected by the user
     * @return the id of the (new or existing) {@link PaymentAttempt}
     */
    @Transactional
    public UUID initiatePayment(UUID orderId, String idempotencyKey, String paymentMethodCode) {

        // ── 1. Idempotency check ─────────────────────────────────────────────────
        var existing = paymentAttemptRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return handleExistingAttempt(existing.get(), idempotencyKey);
        }

        // ── 2. Load and validate order ───────────────────────────────────────────
        Order order = orderReadRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        validateOrderForPayment(order, orderId);

        // ── 3. Create PaymentAttempt (PENDING → PROCESSING atomically) ───────────
        //       DB unique constraint on idempotency_key prevents duplicate rows
        //       if two requests race past step 1.
        PaymentAttempt attempt = PaymentAttempt.create(orderId, idempotencyKey, paymentMethodCode, order.getMoney());
        attempt.markProcessing();
        attempt = paymentAttemptRepository.save(attempt);

        log.info("Payment attempt {} created for order {} (idempotencyKey={})",
            attempt.getId(), orderId, idempotencyKey);

        // ── 4. Notify order aggregate ────────────────────────────────────────────
        commandGateway.sendAndWait(
            new MarkOrderPaymentInProgressCommand(orderId, attempt.getId()), 10, TimeUnit.SECONDS);

        // ── 5. Call payment gateway ──────────────────────────────────────────────
        //       The idempotencyKey is forwarded so the gateway can deduplicate.
        try {
            paymentGatewayPort.charge(orderId, attempt.getId(), idempotencyKey, attempt.getAmount(), attempt.getPaymentMethodCode());
        }
        catch (Exception e) {
            log.error("Payment gateway threw an exception for attempt {} (order {})",
                idempotencyKey, orderId, e);
            String reason = "Gateway error: " + e.getMessage();
            attempt.markFailed(reason);
            paymentAttemptRepository.save(attempt);
            commandGateway.sendAndWait(
                new MarkOrderPaymentFailedCommand(orderId, attempt.getId(), reason), 10, TimeUnit.SECONDS);
            throw new DomainException("Payment gateway error: " + e.getMessage());
        }

        log.info("Payment attempt {} registered with gateway for order {}", attempt.getId(), orderId);
        return attempt.getId();
    }

    @Transactional
    public void completePayment(UUID orderId, UUID paymentAttemptId, String status, String gatewayTransactionId, String failureReason) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(paymentAttemptId)
            .orElseThrow(() -> new EntityNotFoundException("Payment attempt not found: " + paymentAttemptId));
        if (!attempt.getOrderId().equals(orderId)) {
            throw new DomainException("Payment attempt " + paymentAttemptId + " does not belong to order " + orderId);
        }
        if (attempt.getStatus().isTerminal()) {
            log.info("Ignoring duplicate payment callback for terminal attempt {}", paymentAttemptId);
            return;
        }
        if ("SUCCESS".equalsIgnoreCase(status) || "SUCCEEDED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            attempt.markSucceeded(gatewayTransactionId == null || gatewayTransactionId.isBlank()
                ? "MOCK-TXN-" + paymentAttemptId
                : gatewayTransactionId);
            paymentAttemptRepository.save(attempt);
            commandGateway.sendAndWait(new MarkOrderPaidCommand(orderId, paymentAttemptId), 10, TimeUnit.SECONDS);
            log.info("Order {} paid successfully via callback attempt {}", orderId, paymentAttemptId);
            return;
        }
        String reason = failureReason == null || failureReason.isBlank() ? "Payment rejected by gateway" : failureReason;
        attempt.markFailed(reason);
        paymentAttemptRepository.save(attempt);
        commandGateway.sendAndWait(new MarkOrderPaymentFailedCommand(orderId, paymentAttemptId, reason), 10, TimeUnit.SECONDS);
        log.warn("Order {} payment failed via callback attempt {}: {}", orderId, paymentAttemptId, reason);
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    private UUID handleExistingAttempt(PaymentAttempt attempt, String idempotencyKey) {
        switch (attempt.getStatus()) {
        case SUCCEEDED -> {
            log.info("Idempotent: payment attempt {} already succeeded (order {})",
                idempotencyKey, attempt.getOrderId());
            return attempt.getId();
        }
        case PROCESSING -> {
            log.warn("Payment attempt {} is still in progress for order {}",
                idempotencyKey, attempt.getOrderId());
            return attempt.getId();
        }
        case FAILED -> throw new DomainException(
            "Payment attempt already failed with: " + attempt.getFailureReason()
                + ". Submit a new request with a different idempotency key to retry.");
        default -> throw new DomainException("Unexpected attempt status: " + attempt.getStatus());
        }
    }

    private void validateOrderForPayment(Order order, UUID orderId) {
        OrderStatus status = order.getStatus();
        if (status == OrderStatus.PAID) {
            throw new DomainException("Order " + orderId + " is already paid");
        }
        if (status == OrderStatus.CANCELLED || status == OrderStatus.EXPIRED) {
            throw new DomainException("Order " + orderId + " is in a terminal state: " + status);
        }
        if (status != OrderStatus.PENDING_PAYMENT && status != OrderStatus.PAYMENT_FAILED) {
            throw new InvalidOrderStateException(
                "Cannot pay order " + orderId + " in status " + status);
        }
        if (!order.getExpiresAt().isAfter(LocalDateTime.now())) {
            log.warn("Order {} has expired, triggering expiry", orderId);
            commandGateway.sendAndWait(new ExpireOrderCommand(orderId), 10, TimeUnit.SECONDS);
            throw new DomainException("Order " + orderId + " has expired");
        }
    }
}
