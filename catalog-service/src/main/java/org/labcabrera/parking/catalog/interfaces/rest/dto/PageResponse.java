package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.util.List;

public record PageResponse<E>(
    List<E> content,
    Pagination pagination
) {
}
