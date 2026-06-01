package org.labcabrera.parking.catalog.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.labcabrera.parking.catalog.domain.port.outbound.AvailabilityBroadcastPort;
import org.labcabrera.parking.catalog.domain.port.outbound.AvailabilityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityChangeConsumer {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityChangeConsumer.class);

    private final AvailabilityBroadcastPort streamRegistry;
    private final AvailabilityCache availabilityCache;
    private final ObjectMapper objectMapper;

    public AvailabilityChangeConsumer(
            AvailabilityBroadcastPort streamRegistry,
            AvailabilityCache availabilityCache,
            ObjectMapper objectMapper) {
        this.streamRegistry = streamRegistry;
        this.availabilityCache = availabilityCache;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "parking.availability.changes", groupId = "${spring.kafka.consumer.group-id:catalog-service}")
    public void onAvailabilityChange(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String facilityId = payload.path("facilityId").asText();
            if (facilityId.isBlank()) {
                log.warn("Received availability change without facilityId: {}", message);
                return;
            }
            // Invalidate Redis cache for the affected facility
            availabilityCache.invalidate(facilityId);
            // Push SSE event to connected clients
            streamRegistry.send(facilityId, payload);
            log.debug("Availability change processed for facility {}", facilityId);
        } catch (Exception e) {
            log.error("Failed to process availability change message: {}", message, e);
        }
    }
}
