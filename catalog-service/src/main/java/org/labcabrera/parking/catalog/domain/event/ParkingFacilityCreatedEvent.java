package org.labcabrera.parking.catalog.domain.event;

import java.time.Instant;

public record ParkingFacilityCreatedEvent(
    String parkingFacilityId,
    String name,
    String city,
    String address,
    Integer totalSpots,
    Instant occurredAt) {

}
