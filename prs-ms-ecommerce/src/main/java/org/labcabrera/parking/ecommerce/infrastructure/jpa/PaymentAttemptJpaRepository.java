package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.PaymentAttemptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttemptJpaEntity, UUID> {

    Optional<PaymentAttemptJpaEntity> findByIdempotencyKey(String idempotencyKey);

    List<PaymentAttemptJpaEntity> findByOrderId(UUID orderId);
}
