package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.OrderJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID>,
    JpaSpecificationExecutor<OrderJpaEntity> {

    Optional<OrderJpaEntity> findByHoldId(UUID holdId);

    List<OrderJpaEntity> findByStatusInAndExpiresAtLessThanEqual(Collection<OrderStatus> statuses, LocalDateTime expiresAt, Pageable pageable);
}
