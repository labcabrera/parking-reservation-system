package org.labcabrera.parking.reservation.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.eventhandling.annotations.EventHandler;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldExpiredEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldReleasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes availability change events to parking.availability.changes topic.
 * Triggered by HoldCreatedEvent (spot becomes unavailable) and
 * HoldReleasedEvent (spot becomes available again).
 * HoldExpiredEvent is handled in T045.
 */
@Component
public class AvailabilityChangePublisher {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityChangePublisher.class);
    private static final String TOPIC = "parking.availability.changes";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AvailabilityChangePublisher(KafkaTemplate<String, String> kafkaTemplate,
                                        ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @EventHandler
    public void onHoldCreated(HoldCreatedEvent event) {
        publish(event.facilityId(), event.spotId(), "HOLD_CREATED");
    }

    @EventHandler
    public void onHoldReleased(HoldReleasedEvent event) {
        publish(event.facilityId(), event.spotId(), "HOLD_RELEASED");
    }

    @EventHandler
    public void onHoldExpired(HoldExpiredEvent event) {
        publish(event.facilityId(), event.spotId(), "HOLD_EXPIRED");
    }

    public void publish(UUID facilityId, UUID spotId, String eventType) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("facilityId", facilityId.toString());
            payload.put("spotId", spotId.toString());
            payload.put("eventType", eventType);

            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, facilityId.toString(), message);
            log.debug("Availability change published for facility [{}], event: {}", facilityId, eventType);
        } catch (Exception e) {
            log.error("Failed to publish availability change for facility [{}]", facilityId, e);
        }
    }
}
