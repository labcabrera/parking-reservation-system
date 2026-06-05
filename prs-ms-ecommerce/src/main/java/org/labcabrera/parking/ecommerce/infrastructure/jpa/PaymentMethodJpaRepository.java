package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.PaymentMethodJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodJpaEntity, UUID> {

    Optional<PaymentMethodJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    Page<PaymentMethodJpaEntity> findByStatus(PaymentMethodStatus status, Pageable pageable);
}
