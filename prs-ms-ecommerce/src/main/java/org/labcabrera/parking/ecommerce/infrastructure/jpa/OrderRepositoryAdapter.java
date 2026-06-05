package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderReadRepository {

    private final OrderJpaRepository repository;
    private final OrderJpaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return repository.findById(orderId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByHoldId(UUID holdId) {
        return repository.findByHoldId(holdId).map(mapper::toDomain);
    }
}
