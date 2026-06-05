package org.labcabrera.parking.ecommerce.infrastructure.stream;

import java.util.function.Consumer;

import org.labcabrera.parking.ecommerce.application.saga.OrderSaga;
import org.labcabrera.parking.ecommerce.application.saga.ReservationCreatedForOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ReservationCreatedStreamListener {

    @Bean
    public Consumer<ReservationCreatedMessage> reservationCreatedConsumer(OrderSaga orderSaga) {
        return message -> {
            log.info("Received reservation created message {}", message.reservationId());
            orderSaga.on(new ReservationCreatedForOrder(
                message.reservationId(),
                message.amount(),
                message.currency()));
        };
    }
}
