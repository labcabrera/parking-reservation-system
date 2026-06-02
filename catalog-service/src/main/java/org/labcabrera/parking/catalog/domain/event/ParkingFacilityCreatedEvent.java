package org.labcabrera.parking.catalog.domain.event;

import java.time.Instant;

import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;

public record ParkingFacilityCreatedEvent(
    FacilityId facilityId,
    String name,
    String city,
    String address,
    Integer totalSpots,
    Instant occurredAt) {

}
