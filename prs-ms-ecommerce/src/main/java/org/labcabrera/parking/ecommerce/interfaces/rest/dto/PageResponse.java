package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    Pagination pagination) {
}
