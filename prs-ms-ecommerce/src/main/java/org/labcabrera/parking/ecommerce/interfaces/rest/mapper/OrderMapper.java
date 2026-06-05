package org.labcabrera.parking.ecommerce.interfaces.rest.mapper;

import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.OrderDto;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        return new OrderDto(
            order.getId(),
            order.getHoldId(),
            order.getExpiresAt(),
            order.getMoney().amount(),
            order.getMoney().currency(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getLastPaymentAttemptId(),
            order.getFailureReason());
    }
}
