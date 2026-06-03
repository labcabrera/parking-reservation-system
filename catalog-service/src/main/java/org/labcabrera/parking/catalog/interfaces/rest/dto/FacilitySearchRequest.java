package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "FacilitySearchRequest", description = "Request payload for searching parking facilities. Validation: text required, checkIn and checkOut required.")
public record FacilitySearchRequest(

    @NotBlank
    @Schema(description = "Search text for facility name, city or address", examples = "Central Parking", requiredMode = Schema.RequiredMode.REQUIRED)
    String text,

    @NotNull
    @Schema(description = "Check-in datetime for availability search", examples="2026-01-01T15:00:00.000Z", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime checkIn,

    @NotNull
    @Schema(description = "Check-out datetime for availability search", examples="2026-01-02T15:00:00.000Z", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime checkOut,

    @Schema(description = "Maximum number of results to return", examples = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)  
    Integer limit) {
}
