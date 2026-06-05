package org.labcabrera.parking.facilities.infrastructure.kafka;

import org.axonframework.eventhandling.EventHandler;
import org.labcabrera.parking.facilities.domain.event.ParkingFacilityCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParkingFacilityKafkaPublisher {

    //TODO config
    private static final String TOPIC = "parking.facility.events";

    private final KafkaTemplate<String, ParkingFacilityCreatedEvent> kafkaTemplate;

    public ParkingFacilityKafkaPublisher(
        KafkaTemplate<String, ParkingFacilityCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @EventHandler
    public void on(ParkingFacilityCreatedEvent event) {
        log.debug("Publishing event to Kafka: {} ({})", event, TOPIC);
        kafkaTemplate.send(TOPIC, event.facilityId().toString(), event);
    }
}
