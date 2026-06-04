package org.labcabrera.parking.catalog.domain.event;

import java.time.Instant;

import org.labcabrera.parking.catalog.domain.valueobject.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobject.ParkingCapacity;

public record ParkingFacilityCreatedEvent(
    FacilityId facilityId,
    String name,
    String city,
    String address,
    ParkingCapacity capacity,
    Instant occurredAt) {

}
