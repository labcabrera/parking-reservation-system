package org.labcabrera.parking.pricing.interfaces.stream;

import java.util.function.Consumer;

import org.labcabrera.parking.pricing.application.event.UpdatedParkingFacilityCapacityEvent;
import org.labcabrera.parking.pricing.application.service.ParkingFacilityCapacityProjectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class UpdatedParkingFacilityCapacityEventConsumer {

    @Bean
    public Consumer<UpdatedParkingFacilityCapacityEvent> updatedParkingFacilityCapacityConsumer(
        ParkingFacilityCapacityProjectionService service) {
        return event -> {
            log.info(
                "Received UpdatedParkingFacilityCapacityEvent for parking facility {} and slot type {}",
                event.parkingFacilityId(),
                event.slotType());
            service.handle(event);
        };
    }
}
