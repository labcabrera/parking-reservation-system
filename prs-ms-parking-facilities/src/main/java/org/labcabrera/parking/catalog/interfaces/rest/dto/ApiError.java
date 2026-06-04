package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "Standard API error response")
public record ApiError(

    @Schema(description = "Error code", examples = "BAD_REQUEST", requiredMode = Schema.RequiredMode.REQUIRED)
    String code,
    
    @Schema(description = "Error message", examples = "Invalid request parameters", requiredMode = Schema.RequiredMode.REQUIRED)
    String message,
    
    @Schema(description = "Timestamp of the error occurrence", examples = "2024-06-01T12:00:00.000Z", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime timestamp,
    
    @Schema(description = "Detailed error messages", examples = "[\"'name' must not be blank\", \"'totalSpots' must be greater than or equal to 1\"]",  requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<String> details) {
}
