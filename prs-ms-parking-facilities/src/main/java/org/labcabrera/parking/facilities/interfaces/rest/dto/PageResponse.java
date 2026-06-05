package org.labcabrera.parking.facilities.interfaces.rest.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PageResponse", description = "Generic paginated response wrapper")
public record PageResponse<E>(

    @Schema(description = "List of items in the current page") List<E> content,

    @Schema(description = "Pagination metadata") Pagination pagination) {
}
