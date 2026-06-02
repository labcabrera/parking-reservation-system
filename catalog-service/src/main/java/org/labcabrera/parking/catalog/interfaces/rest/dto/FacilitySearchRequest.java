package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FacilitySearchRequest(
    @NotBlank String text,
    @NotNull LocalDateTime checkIn,
    @NotNull LocalDateTime checkOut,
    Integer limit) {
}
