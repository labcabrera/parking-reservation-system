package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
    String code,
    String message,
    LocalDateTime timestamp,
    List<String> details) {
}
