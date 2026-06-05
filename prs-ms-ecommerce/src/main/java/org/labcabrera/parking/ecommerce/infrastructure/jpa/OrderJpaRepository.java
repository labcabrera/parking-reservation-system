package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByHoldId(UUID holdId);
}
