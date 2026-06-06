package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.PaymentAttemptRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentAttempt;
import org.labcabrera.parking.ecommerce.domain.valueobject.Money;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.MoneyEmbeddable;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.PaymentAttemptJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PaymentAttemptRepositoryAdapter implements PaymentAttemptRepository {

    private final PaymentAttemptJpaRepository repository;

    @Override
    @Transactional
    public PaymentAttempt save(PaymentAttempt attempt) {
        PaymentAttemptJpaEntity entity = toEntity(attempt);
        entity = repository.save(entity);
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentAttempt> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAttempt> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).stream().map(this::toDomain).toList();
    }

    private PaymentAttemptJpaEntity toEntity(PaymentAttempt domain) {
        PaymentAttemptJpaEntity entity = new PaymentAttemptJpaEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrderId());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        entity.setPaymentMethodCode(domain.getPaymentMethodCode());
        entity.setAmount(new MoneyEmbeddable(
            domain.getAmount().amount(), domain.getAmount().currency()));
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setProcessedAt(domain.getProcessedAt());
        entity.setGatewayTransactionId(domain.getGatewayTransactionId());
        entity.setFailureReason(domain.getFailureReason());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    private PaymentAttempt toDomain(PaymentAttemptJpaEntity entity) {
        return new PaymentAttempt(
            entity.getId(),
            entity.getOrderId(),
            entity.getIdempotencyKey(),
            entity.getPaymentMethodCode(),
            new Money(entity.getAmount().getAmount(), entity.getAmount().getCurrency()),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getProcessedAt(),
            entity.getGatewayTransactionId(),
            entity.getFailureReason(),
            entity.getVersion());
    }
}
