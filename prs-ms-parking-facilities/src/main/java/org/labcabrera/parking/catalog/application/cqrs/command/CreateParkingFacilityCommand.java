package org.labcabrera.parking.catalog.application.cqrs.command;

import java.util.Set;

import org.labcabrera.parking.catalog.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.valueobject.Coordinates;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityTag;
import org.labcabrera.parking.catalog.domain.valueobject.ParkingPricingRule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateParkingFacilityCommand(
    
    @NotBlank
    String name,
    
    @NotBlank
    String city,
    
    @NotBlank
    String address,
    
    @NotNull
    Coordinates location,
    
    @Min(1)
    int totalSpots,
    
    Set<FacilityTag> tags,
    
    @NotNull
    FacilityStatus status,
    
    @Valid
    CancellationPolicy cancellationPolicy,
    
    @Valid
    ParkingPricingRule pricingRule) {

}
