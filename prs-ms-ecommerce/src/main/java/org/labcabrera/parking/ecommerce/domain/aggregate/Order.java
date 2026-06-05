package org.labcabrera.parking.ecommerce.domain.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.LocalDateTime;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.labcabrera.parking.ecommerce.application.cqrs.command.CreateOrderCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.ExpireOrderCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.MarkOrderPaidCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.MarkOrderPaymentFailedCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.MarkOrderPaymentInProgressCommand;
import org.labcabrera.parking.ecommerce.domain.event.OrderCreatedEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderExpiredEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaidEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaymentFailedEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaymentInProgressEvent;
import org.labcabrera.parking.ecommerce.domain.exception.InvalidOrderStateException;
import org.labcabrera.parking.ecommerce.domain.valueobject.Money;
import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aggregate(repository = "orderRepository")
@Entity
@Table(name = "orders", schema = "ecommerce")
@NoArgsConstructor
@Getter
@Slf4j
public class Order {

    @Id
    @AggregateIdentifier
    private UUID id;

    @Column(name = "hold_id", nullable = false, unique = true)
    private UUID holdId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Embedded
    private Money money;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_payment_attempt_id")
    private UUID lastPaymentAttemptId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Version
    private Long version;

    @CommandHandler
    public Order(CreateOrderCommand command) {
        log.info("Creating order {} from hold {}", command.orderId(), command.holdId());
        apply(new OrderCreatedEvent(
            command.orderId(),
            command.holdId(),
            command.expiresAt(),
            command.amount(),
            command.currency(),
            LocalDateTime.now()));
    }

    @EventSourcingHandler
    void on(OrderCreatedEvent event) {
        this.id = event.orderId();
        this.holdId = event.holdId();
        this.expiresAt = event.expiresAt();
        this.money = new Money(event.amount(), event.currency());
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = event.occurredAt();
    }

    @CommandHandler
    void handle(MarkOrderPaymentInProgressCommand command) {
        if (status != OrderStatus.PENDING_PAYMENT && status != OrderStatus.PAYMENT_FAILED) {
            throw new InvalidOrderStateException("Cannot start payment from status " + status);
        }
        if (isExpired()) {
            apply(new OrderExpiredEvent(command.orderId()));
            return;
        }
        apply(new OrderPaymentInProgressEvent(command.orderId(), command.paymentAttemptId()));
    }

    @EventSourcingHandler
    void on(OrderPaymentInProgressEvent event) {
        this.status = OrderStatus.PAYMENT_IN_PROGRESS;
        this.lastPaymentAttemptId = event.paymentAttemptId();
        this.failureReason = null;
    }

    @CommandHandler
    void handle(MarkOrderPaidCommand command) {
        if (status != OrderStatus.PAYMENT_IN_PROGRESS) {
            throw new InvalidOrderStateException("Only PAYMENT_IN_PROGRESS orders can be paid (current: " + status + ")");
        }
        apply(new OrderPaidEvent(command.orderId(), command.paymentAttemptId()));
    }

    @EventSourcingHandler
    void on(OrderPaidEvent event) {
        this.status = OrderStatus.PAID;
        this.lastPaymentAttemptId = event.paymentAttemptId();
        this.failureReason = null;
    }

    @CommandHandler
    void handle(MarkOrderPaymentFailedCommand command) {
        if (status.isTerminal()) {
            return;
        }
        apply(new OrderPaymentFailedEvent(command.orderId(), command.paymentAttemptId(), command.reason()));
    }

    @EventSourcingHandler
    void on(OrderPaymentFailedEvent event) {
        this.status = OrderStatus.PAYMENT_FAILED;
        this.lastPaymentAttemptId = event.paymentAttemptId();
        this.failureReason = event.reason();
    }

    @CommandHandler
    void handle(ExpireOrderCommand command) {
        if (status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAYMENT_FAILED) {
            apply(new OrderExpiredEvent(command.orderId()));
        }
    }

    @EventSourcingHandler
    void on(OrderExpiredEvent event) {
        this.status = OrderStatus.EXPIRED;
    }

    private boolean isExpired() {
        return !expiresAt.isAfter(LocalDateTime.now());
    }
}
