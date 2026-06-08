package org.labcabrera.parking.ecommerce.interfaces.stream;

import java.util.function.Consumer;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.labcabrera.parking.ecommerce.application.saga.ReservationCreatedForOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ReservationCreatedStreamListener {

    @Bean
    public Consumer<ReservationCreatedMessage> reservationCreatedConsumer(EventGateway eventGateway) {
        return message -> {
            log.info("Received reservation created message {}", message.reservationId());
            eventGateway.publish(new ReservationCreatedForOrder(
                message.reservationId(),
                message.bookingSessionId(),
                message.expiresAt(),
                message.amount(),
                message.currency()));
        };
    }
}
