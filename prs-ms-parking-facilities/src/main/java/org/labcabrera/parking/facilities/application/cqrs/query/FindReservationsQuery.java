package org.labcabrera.parking.facilities.application.cqrs.query;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.NotNull;

public record FindReservationsQuery(

    @NotNull LocalDateTime start,

    @NotNull LocalDateTime end,

    UUID facilityId,

    @NotNull Pageable pageable) {

    public FindReservationsQuery {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }

}
