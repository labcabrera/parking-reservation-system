package org.labcabrera.parking.reservation.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.labcabrera.parking.reservation.domain.port.outbound.PricingRequestPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka implementation of PricingRequestPort.
 * Publishes pricing requests to parking.pricing.requests topic.
 */
@Component
public class PricingRequestPublisher implements PricingRequestPort {

    private static final Logger log = LoggerFactory.getLogger(PricingRequestPublisher.class);
    private static final String TOPIC = "parking.pricing.requests";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PricingRequestPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                    ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(UUID holdId, UUID facilityId, UUID spotId,
                        BigDecimal baseRatePerDay, String currency,
                        Instant checkIn, Instant checkOut) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("holdId", holdId.toString());
            payload.put("facilityId", facilityId.toString());
            payload.put("spotId", spotId.toString());
            payload.put("baseRatePerDay", baseRatePerDay);
            payload.put("currency", currency);
            payload.put("checkIn", LocalDateTime.ofInstant(checkIn, ZoneOffset.UTC).toString());
            payload.put("checkOut", LocalDateTime.ofInstant(checkOut, ZoneOffset.UTC).toString());

            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, holdId.toString(), message);
            log.info("Pricing request published for hold [{}]", holdId);
        } catch (Exception e) {
            log.error("Failed to publish pricing request for hold [{}]", holdId, e);
            throw new RuntimeException("Pricing request publish failed", e);
        }
    }
}
