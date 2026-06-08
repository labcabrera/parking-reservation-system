package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
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

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAll(OrderStatus status, UUID holdId, Pageable pageable) {
        Specification<OrderJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (holdId != null) {
                predicates.add(cb.equal(root.get("holdId"), holdId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(mapper::toDomain);
    }
}
