package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.util.Set;

import org.labcabrera.parking.catalog.domain.valueobjects.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.valueobjects.Coordinates;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityTag;
import org.labcabrera.parking.catalog.domain.valueobjects.ParkingPricingRule;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ParkingFacility", description = "Parking facility details")
public record ParkingFacilityDto(
    @Schema(description = "Unique parking facility identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String id,

    @Schema(description = "Facility name", example = "Central Parking") String name,

    @Schema(description = "City where the facility is located", example = "Madrid") String city,

    @Schema(description = "Street address of the facility", example = "Calle Mayor 1") String address,

    @Schema(description = "Geographic coordinates of the facility") Coordinates location,

    @Schema(description = "Total number of parking spots", example = "120") int totalSpots,

    @Schema(description = "Tags associated with the facility (enum)") Set<FacilityTag> tags,

    @Schema(description = "Current facility status") FacilityStatus status,

    @Schema(description = "Cancellation policy for the facility") CancellationPolicy cancellationPolicy,

    @Schema(description = "Pricing rule information for the facility") ParkingPricingRule pricingRule) {

}
