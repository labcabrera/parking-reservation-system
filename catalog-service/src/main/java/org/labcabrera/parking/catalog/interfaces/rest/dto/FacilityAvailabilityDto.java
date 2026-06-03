package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FacilityAvailability", description = "Availability information for a parking facility over a requested interval")
public record FacilityAvailabilityDto(

    @Schema(description = "Unique facility id", examples = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID id,

    @Schema(description = "Facility name", examples = "Central Parking")
    String name,

    @Schema(description = "City", examples = "Madrid")
    String city,

    @Schema(description = "Address", examples = "Calle Mayor 1")
    String address,

    @Schema(description = "Total number of parking spots", examples = "120")
    int totalSpots,

    @Schema(description = "Number of free spots in the requested interval", examples = "5")
    int availableSpots,

    @Schema(description = "If true, availability is considered low (near capacity)")
    boolean lowAvailability,

    @Schema(description = "Estimated price for the requested interval", examples = "12.50")
    BigDecimal estimatedPrice,

    @Schema(description = "Currency code", examples = "EUR")
    String currency) {
}
