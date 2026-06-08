package org.labcabrera.parking.ecommerce.interfaces.stream;

import java.time.Instant;

import org.axonframework.eventhandling.EventHandler;
import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.event.OrderExpiredEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentExpiredStreamPublisher {

    static final String BINDING_NAME = "orderPaymentExpired-out-0";
    private static final String EVENT_VERSION = "v1";

    private final OrderReadRepository orderReadRepository;
    private final StreamBridge streamBridge;

    @EventHandler
    public void on(OrderExpiredEvent event) {
        orderReadRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            OrderPaymentExpiredMessage payload = new OrderPaymentExpiredMessage(
                order.getId(),
                order.getHoldId(),
                order.getExpiresAt(),
                Instant.now(),
                EVENT_VERSION);

            boolean sent = streamBridge.send(
                BINDING_NAME,
                MessageBuilder.withPayload(payload)
                    .setHeader(KafkaHeaders.KEY, order.getHoldId().toString())
                    .build());

            if (!sent) {
                log.warn("Order payment expired message was not accepted by binding {} for order {}",
                    BINDING_NAME, order.getId());
            }
            else {
                log.info("Published order payment expired message for order {} and reservation {}",
                    order.getId(), order.getHoldId());
            }
        }, () -> log.warn("Cannot publish order payment expired message; order {} not found", event.orderId()));
    }
}
