package org.labcabrera.parking.ecommerce.application.port;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderReadRepository {

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByHoldId(UUID holdId);

    Page<Order> findAll(OrderStatus status, UUID holdId, Pageable pageable);
}
