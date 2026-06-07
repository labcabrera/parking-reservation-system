package org.labcabrera.parking.facilities.interfaces.rest.dto;

import java.util.Set;

import org.labcabrera.parking.facilities.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.facilities.domain.valueobject.Coordinates;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityTag;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingPricingRule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateParkingFacilityRequest", description = "Request payload to replace a parking facility.")
public record UpdateParkingFacilityRequest(

    @NotBlank @Schema(description = "Facility name", examples = "Airport Parking T1") String name,

    @NotBlank @Schema(description = "City where the facility is located", examples = "Madrid") String city,

    @NotBlank @Schema(description = "Street address", examples = "Terminal 1") String address,

    @NotNull @Valid @Schema(description = "Geographical coordinates", requiredMode = Schema.RequiredMode.REQUIRED) Coordinates location,

    @NotNull @Valid @Schema(description = "Parking capacity", requiredMode = Schema.RequiredMode.REQUIRED) ParkingCapacity capacity,

    @Schema(description = "Tags associated with the facility") Set<FacilityTag> tags,

    @NotNull @Schema(description = "Facility operational status", requiredMode = Schema.RequiredMode.REQUIRED) FacilityStatus status,

    @Valid @Schema(description = "Cancellation policy") CancellationPolicy cancellationPolicy,

    @NotNull @Valid @Schema(description = "Pricing rule information for the facility", requiredMode = Schema.RequiredMode.REQUIRED) ParkingPricingRule pricingRule) {
}
