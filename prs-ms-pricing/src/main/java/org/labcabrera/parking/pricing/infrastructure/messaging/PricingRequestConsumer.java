package org.labcabrera.parking.pricing.infrastructure.messaging;

import org.labcabrera.parking.pricing.domain.service.PricingCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.time.temporal.ChronoUnit;

@Component
public class PricingRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(PricingRequestConsumer.class);

    private final PricingCalculationService pricingService;
    private final PricingResultPublisher resultPublisher;
    private final ObjectMapper objectMapper;

    public PricingRequestConsumer(
            PricingCalculationService pricingService,
            PricingResultPublisher resultPublisher,
            ObjectMapper objectMapper) {
        this.pricingService = pricingService;
        this.resultPublisher = resultPublisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "parking.pricing.requests", groupId = "${spring.kafka.consumer.group-id:pricing-service}")
    public void onPricingRequest(String message) {
        PricingRequestMessage request = null;
        try {
            request = objectMapper.readValue(message, PricingRequestMessage.class);
            long days = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
            var confirmedPrice = pricingService.calculate(request.baseRatePerDay(), (int) days);
            var result = PricingResultMessage.success(request.holdId(), confirmedPrice, request.currency());
            resultPublisher.publish(result);
            log.info("Pricing calculated for hold {}: {} {}", request.holdId(), confirmedPrice, request.currency());
        } catch (Exception e) {
            log.error("Failed to process pricing request: {}", message, e);
            if (request != null) {
                resultPublisher.publish(PricingResultMessage.failure(request.holdId(), e.getMessage()));
            }
        }
    }
}
