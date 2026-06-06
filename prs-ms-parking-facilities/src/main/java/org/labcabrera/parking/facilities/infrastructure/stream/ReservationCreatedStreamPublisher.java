package org.labcabrera.parking.facilities.infrastructure.stream;

import java.time.Instant;

import org.axonframework.eventhandling.EventHandler;
import org.labcabrera.parking.facilities.domain.event.ReservationConfirmedEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedStreamPublisher {

    static final String BINDING_NAME = "reservationCreated-out-0";
    private static final String EVENT_VERSION = "v1";

    private final StreamBridge streamBridge;

    @EventHandler
    public void on(ReservationConfirmedEvent event) {
        ReservationCreatedMessage payload = new ReservationCreatedMessage(
            event.reservationId(),
            event.facilityId(),
            event.userId(),
            event.checkIn(),
            event.checkOut(),
            event.expiresAt(),
            event.estimatedPrice(),
            event.currency(),
            Instant.now(),
            EVENT_VERSION);

        try {
            boolean sent = streamBridge.send(
                BINDING_NAME,
                MessageBuilder.withPayload(payload)
                    .setHeader(KafkaHeaders.KEY, event.reservationId().toString())
                    .build());
            if (!sent) {
                log.warn("Reservation created message was not accepted by binding {} for reservation {}",
                    BINDING_NAME, event.reservationId());
            }
            else {
                log.info("Published reservation created message for reservation {}", event.reservationId());
            }
        }
        catch (RuntimeException ex) {
            log.error("Error publishing reservation created message for reservation {}", event.reservationId(), ex);
        }
    }
}
