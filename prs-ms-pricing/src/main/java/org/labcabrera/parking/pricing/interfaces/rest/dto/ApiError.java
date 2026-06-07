package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "Error response returned by the pricing API")
public record ApiError(
    @Schema(description = "Error timestamp", example = "2026-06-07T10:15:30Z")
    Instant timestamp,
    @Schema(description = "HTTP status code", example = "400")
    int status,
    @Schema(description = "HTTP status reason", example = "Bad Request")
    String error,
    @Schema(description = "Human-readable error message", example = "Validation failed")
    String message,
    @Schema(description = "Request path", example = "/api/v1/pricing/dynamic-rate")
    String path,
    @Schema(description = "Field-level or validation details")
    List<String> details) {
}
