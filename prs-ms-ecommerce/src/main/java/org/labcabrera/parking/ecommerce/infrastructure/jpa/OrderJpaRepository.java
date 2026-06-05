package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByHoldId(UUID holdId);
}
