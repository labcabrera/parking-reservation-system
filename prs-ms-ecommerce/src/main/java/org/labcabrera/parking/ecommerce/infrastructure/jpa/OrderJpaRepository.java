package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID>,
    JpaSpecificationExecutor<OrderJpaEntity> {

    Optional<OrderJpaEntity> findByHoldId(UUID holdId);
}
