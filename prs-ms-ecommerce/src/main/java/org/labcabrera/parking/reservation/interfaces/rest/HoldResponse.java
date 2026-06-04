package org.labcabrera.parking.reservation.interfaces.rest;

import org.labcabrera.parking.reservation.domain.model.HoldReadModel;
import org.labcabrera.parking.reservation.domain.model.HoldStatus;
import org.labcabrera.parking.reservation.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for hold queries.
 */
public record HoldResponse(
        UUID holdId,
        String searchSessionId,
        UUID spotId,
        UUID facilityId,
        HoldStatus status,
        Money estimatedPrice,
        Money confirmedPrice,
        Instant checkIn,
        Instant checkOut,
        Instant expiresAt) {

    public static HoldResponse from(HoldReadModel model) {
        return new HoldResponse(
                model.holdId(),
                model.searchSessionId(),
                model.spotId(),
                model.facilityId(),
                model.status(),
                model.estimatedPrice(),
                model.confirmedPrice(),
                model.checkIn(),
                model.checkOut(),
                model.expiresAt()
        );
    }
}
