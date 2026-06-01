package org.labcabrera.parking.reservation.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.reservation.application.commands.ConfirmHoldPriceCommand;
import org.labcabrera.parking.reservation.application.commands.FailHoldPricingCommand;
import org.labcabrera.parking.reservation.domain.model.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Consumes pricing results from parking.pricing.results topic.
 * Dispatches ConfirmHoldPriceCommand or FailHoldPricingCommand to the command bus.
 */
@Component
public class PricingResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(PricingResultConsumer.class);

    private final CommandGateway commandGateway;
    private final ObjectMapper objectMapper;

    public PricingResultConsumer(CommandGateway commandGateway, ObjectMapper objectMapper) {
        this.commandGateway = commandGateway;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "parking.pricing.results", groupId = "${spring.kafka.consumer.group-id:reservation-service}")
    public void onPricingResult(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            UUID holdId = UUID.fromString(payload.path("holdId").asText());
            String status = payload.path("status").asText();

            if ("SUCCESS".equalsIgnoreCase(status)) {
                BigDecimal confirmedAmount = new BigDecimal(payload.path("confirmedPrice").asText());
                String currency = payload.path("currency").asText("EUR");
                var cmd = new ConfirmHoldPriceCommand(holdId, new Money(confirmedAmount, currency));
                commandGateway.sendAndWait(cmd);
                log.info("Confirmed price for hold [{}]: {} {}", holdId, confirmedAmount, currency);
            } else {
                String reason = payload.path("failReason").asText("Pricing failed");
                commandGateway.sendAndWait(new FailHoldPricingCommand(holdId, reason));
                log.warn("Pricing failed for hold [{}]: {}", holdId, reason);
            }
        } catch (Exception e) {
            log.error("Failed to process pricing result: {}", message, e);
        }
    }
}
