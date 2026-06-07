package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PageResponse", description = "Generic paginated response wrapper")
public record PageResponse<E>(

    @Schema(description = "List of items in the current page") List<E> content,

    @Schema(description = "Pagination metadata") Pagination pagination) {

    public static <E> PageResponse<E> from(Page<E> page) {
        return new PageResponse<>(
            page.getContent(),
            new Pagination(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()));
    }
}
