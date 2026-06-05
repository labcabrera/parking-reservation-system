package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.port.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return repository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByHoldId(UUID holdId) {
        return repository.findByHoldId(holdId);
    }
}
