package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.util.Set;

import org.labcabrera.parking.catalog.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.valueobject.Coordinates;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityTag;
import org.labcabrera.parking.catalog.domain.valueobject.ParkingPricingRule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateParkingFacilityRequest", description = "Request payload to create a parking facility. Validation: name required, totalSpots >= 1, pricingRule.estimatedDailyPrice > 0.")
public record CreateParkingFacilityRequest(
    
    @NotBlank
    @Schema(description = "Facility name", examples = "Central Parking", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "City where the facility is located", examples = "Madrid", requiredMode = Schema.RequiredMode.REQUIRED)
    String city,

    @Schema(description = "Street address of the facility", examples = "Calle Mayor 1", requiredMode = Schema.RequiredMode.REQUIRED)
    String address,

    @NotNull
    @Valid
    @Schema(description = "Geographic coordinates of the facility", requiredMode = Schema.RequiredMode.REQUIRED)
    Coordinates location,

    @Min(1)
    @Schema(description = "Total number of parking spots", examples = "120", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    int totalSpots,

    @Schema(description = "Tags associated with the facility (enum)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Set<FacilityTag> tags,

    @Schema(description = "Initial facility status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    FacilityStatus status,

    @Schema(description = "Cancellation policy for the facility", requiredMode = Schema.RequiredMode.REQUIRED)
    CancellationPolicy cancellationPolicy,

    @NotNull
    @Valid
    @Schema(description = "Pricing rule information for the facility", requiredMode = Schema.RequiredMode.REQUIRED)
    ParkingPricingRule pricingRule) {

}
