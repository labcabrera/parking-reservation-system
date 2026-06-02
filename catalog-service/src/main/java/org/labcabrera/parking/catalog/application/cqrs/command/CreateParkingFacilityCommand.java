package org.labcabrera.parking.catalog.application.cqrs.command;

import java.util.Set;

import org.labcabrera.parking.catalog.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.valueobject.Coordinates;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityTag;
import org.labcabrera.parking.catalog.domain.valueobject.ParkingPricingRule;

public record CreateParkingFacilityCommand(
    String name,
    String city,
    String address,
    Coordinates location,
    int totalSpots,
    Set<FacilityTag> tags,
    FacilityStatus status,
    CancellationPolicy cancellationPolicy,
    ParkingPricingRule pricingRule) {

}
