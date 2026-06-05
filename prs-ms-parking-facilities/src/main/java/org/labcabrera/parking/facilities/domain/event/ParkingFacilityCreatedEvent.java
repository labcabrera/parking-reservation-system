package org.labcabrera.parking.facilities.domain.event;

import java.time.Instant;

import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;

public record ParkingFacilityCreatedEvent(
    FacilityId facilityId,
    String name,
    String city,
    String address,
    ParkingCapacity capacity,
    Instant occurredAt) {

}
