package org.labcabrera.parking.pricing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PricingResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(PricingResultPublisher.class);
    private static final String TOPIC = "parking.pricing.results";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PricingResultPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(PricingResultMessage result) {
        try {
            String payload = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(TOPIC, result.holdId().toString(), payload);
            log.debug("Published pricing result for hold {}", result.holdId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize pricing result for hold {}", result.holdId(), e);
        }
    }
}
