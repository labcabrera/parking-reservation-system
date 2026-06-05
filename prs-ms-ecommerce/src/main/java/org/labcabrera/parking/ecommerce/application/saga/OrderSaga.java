package org.labcabrera.parking.ecommerce.application.saga;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.ecommerce.application.cqrs.command.CreateOrderCommand;
import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSaga {

    private final CommandGateway commandGateway;
    private final OrderReadRepository orderReadRepository;

    @Value("${ecommerce.order.ttl-minutes:15}")
    private long orderTtlMinutes;

    public void on(ReservationCreatedForOrder event) {
        UUID holdId = event.reservationId();
        orderReadRepository.findByHoldId(holdId).ifPresentOrElse(
            existing -> log.info("Order {} already exists for reservation {}; skipping duplicate event",
                existing.getId(), holdId),
            () -> createOrder(event, holdId));
    }

    private void createOrder(ReservationCreatedForOrder event, UUID holdId) {
        UUID orderId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(orderTtlMinutes);
        log.info("Creating order {} from reservation {}", orderId, holdId);
        commandGateway.sendAndWait(new CreateOrderCommand(
            orderId,
            holdId,
            expiresAt,
            event.amount(),
            event.currency()), 10, TimeUnit.SECONDS);
    }
}
