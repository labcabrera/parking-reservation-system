package org.labcabrera.parking.ecommerce.domain.port;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;

public interface OrderRepository {

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByHoldId(UUID holdId);
}
