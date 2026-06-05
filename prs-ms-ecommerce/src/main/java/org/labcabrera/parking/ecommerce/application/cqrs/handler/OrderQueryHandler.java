package org.labcabrera.parking.ecommerce.application.cqrs.handler;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.ecommerce.application.cqrs.query.GetOrderByIdQuery;
import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderQueryHandler {

    private final OrderReadRepository repository;

    @QueryHandler
    public Order handle(GetOrderByIdQuery query) {
        return repository.findById(query.orderId())
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + query.orderId()));
    }
}
