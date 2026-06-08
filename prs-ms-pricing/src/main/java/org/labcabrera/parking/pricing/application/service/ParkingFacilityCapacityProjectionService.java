package org.labcabrera.parking.pricing.application.service;

import org.labcabrera.parking.pricing.application.event.UpdatedParkingFacilityCapacityEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParkingFacilityCapacityProjectionService {

    public void handle(UpdatedParkingFacilityCapacityEvent event) {
        log.info("TODO: create price projection");
    }
}
