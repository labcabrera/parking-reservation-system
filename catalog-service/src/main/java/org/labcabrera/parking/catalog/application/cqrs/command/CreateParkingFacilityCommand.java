package org.labcabrera.parking.catalog.application.cqrs.command;

import java.util.Set;

import org.labcabrera.parking.catalog.domain.valueobjects.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.valueobjects.Coordinates;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityTag;
import org.labcabrera.parking.catalog.domain.valueobjects.ParkingPricingRule;

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
