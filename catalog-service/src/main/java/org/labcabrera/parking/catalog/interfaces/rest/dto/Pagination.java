package org.labcabrera.parking.catalog.interfaces.rest.dto;

public record Pagination(
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
