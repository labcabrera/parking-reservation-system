package org.labcabrera.parking.facilities.infrastructure.stream;

import java.util.function.Consumer;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.facilities.application.cqrs.command.ExpireReservationPaymentCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OrderPaymentExpiredStreamListener {

    @Bean
    public Consumer<OrderPaymentExpiredMessage> orderPaymentExpiredConsumer(CommandGateway commandGateway) {
        return message -> {
            log.info("Received order payment expired message for order {} and reservation {}",
                message.orderId(), message.reservationId());
            commandGateway.send(new ExpireReservationPaymentCommand(message.reservationId(), message.orderId()));
        };
    }
}
