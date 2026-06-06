package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

public record Pagination(
    int page,
    int size,
    long totalElements,
    int totalPages) {
}
