package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.valueobject.Money;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.OrderJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderJpaMapper {

    default Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Order(
            entity.getId(),
            entity.getHoldId(),
            entity.getBookingSessionId(),
            entity.getExpiresAt(),
            new Money(entity.getMoney().getAmount(), entity.getMoney().getCurrency()),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getLastPaymentAttemptId(),
            entity.getFailureReason(),
            entity.getVersion());
    }
}
