package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.labcabrera.parking.ecommerce.domain.event.OrderCreatedEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderExpiredEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaidEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaymentFailedEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaymentInProgressEvent;
import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.MoneyEmbeddable;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.OrderJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@ProcessingGroup("order-projection")
@RequiredArgsConstructor
class OrderProjection {

    private final OrderJpaRepository repository;

    @EventHandler
    @Transactional
    public void on(OrderCreatedEvent event) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(event.orderId());
        entity.setHoldId(event.holdId());
        entity.setExpiresAt(event.expiresAt());
        entity.setMoney(new MoneyEmbeddable(event.amount(), event.currency()));
        entity.setStatus(OrderStatus.PENDING_PAYMENT);
        entity.setCreatedAt(event.occurredAt());
        repository.save(entity);
    }

    @EventHandler
    @Transactional
    public void on(OrderPaymentInProgressEvent event) {
        repository.findById(event.orderId()).ifPresent(entity -> {
            entity.setStatus(OrderStatus.PAYMENT_IN_PROGRESS);
            entity.setLastPaymentAttemptId(event.paymentAttemptId());
            entity.setFailureReason(null);
            repository.save(entity);
        });
    }

    @EventHandler
    @Transactional
    public void on(OrderPaidEvent event) {
        repository.findById(event.orderId()).ifPresent(entity -> {
            entity.setStatus(OrderStatus.PAID);
            entity.setLastPaymentAttemptId(event.paymentAttemptId());
            entity.setFailureReason(null);
            repository.save(entity);
        });
    }

    @EventHandler
    @Transactional
    public void on(OrderPaymentFailedEvent event) {
        repository.findById(event.orderId()).ifPresent(entity -> {
            entity.setStatus(OrderStatus.PAYMENT_FAILED);
            entity.setLastPaymentAttemptId(event.paymentAttemptId());
            entity.setFailureReason(event.reason());
            repository.save(entity);
        });
    }

    @EventHandler
    @Transactional
    public void on(OrderExpiredEvent event) {
        repository.findById(event.orderId()).ifPresent(entity -> {
            entity.setStatus(OrderStatus.EXPIRED);
            repository.save(entity);
        });
    }
}
