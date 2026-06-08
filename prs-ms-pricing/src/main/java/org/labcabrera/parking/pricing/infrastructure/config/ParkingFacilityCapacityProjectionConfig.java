package org.labcabrera.parking.pricing.infrastructure.config;

import org.labcabrera.parking.pricing.application.service.ParkingFacilityCapacityProjectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParkingFacilityCapacityProjectionConfig {

    @Bean
    public ParkingFacilityCapacityProjectionService parkingFacilityCapacityProjectionService() {
        return new ParkingFacilityCapacityProjectionService();
    }
}
