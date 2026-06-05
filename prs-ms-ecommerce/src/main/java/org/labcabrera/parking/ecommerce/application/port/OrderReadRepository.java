package org.labcabrera.parking.ecommerce.application.port;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;

public interface OrderReadRepository {

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByHoldId(UUID holdId);
}
