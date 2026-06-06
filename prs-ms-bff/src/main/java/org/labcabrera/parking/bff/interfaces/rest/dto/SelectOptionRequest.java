package org.labcabrera.parking.bff.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request to select a parking option and start a checkout session (hold).
 */
public record SelectOptionRequest(

    @NotNull UUID facilityId,

    @NotNull LocalDateTime checkIn,

    @NotNull LocalDateTime checkOut) {
}
