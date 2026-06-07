package org.labcabrera.parking.facilities.application.cqrs.command;

import java.util.Set;

import org.labcabrera.parking.facilities.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.facilities.domain.valueobject.Coordinates;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityTag;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingPricingRule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateParkingFacilityCommand(

    @NotNull FacilityId facilityId,

    @NotBlank String name,

    @NotBlank String city,

    @NotBlank String address,

    @NotNull Coordinates location,

    @NotNull @Valid ParkingCapacity capacity,

    Set<FacilityTag> tags,

    @NotNull FacilityStatus status,

    @Valid CancellationPolicy cancellationPolicy,

    @NotNull @Valid ParkingPricingRule pricingRule) {
}
