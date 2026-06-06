package org.labcabrera.parking.ecommerce.application.cqrs.query;

import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindOrdersQuery(
    OrderStatus status,
    UUID holdId,
    Pageable pageable) {
}
