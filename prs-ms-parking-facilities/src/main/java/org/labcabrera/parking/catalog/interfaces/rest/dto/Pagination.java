package org.labcabrera.parking.catalog.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Pagination", description = "Pagination metadata for paginated responses")
public record Pagination(

    @Schema(description = "Current page number (0-based index)", example = "0")
    int page,

    @Schema(description = "Number of items per page", example = "20")
    int size,

    @Schema(description = "Total number of items across all pages", example = "100")
    long totalElements,

    @Schema(description = "Total number of pages available", example = "5")
    int totalPages
) {
}
