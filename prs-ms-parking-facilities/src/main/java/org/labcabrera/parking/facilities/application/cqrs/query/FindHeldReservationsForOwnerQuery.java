package org.labcabrera.parking.facilities.application.cqrs.query;

import java.util.UUID;

public record FindHeldReservationsForOwnerQuery(
    String userId,
    String bookingSessionId,
    UUID excludedReservationId) {

    public FindHeldReservationsForOwnerQuery {
        if ((userId == null || userId.isBlank()) && (bookingSessionId == null || bookingSessionId.isBlank())) {
            throw new IllegalArgumentException("userId or bookingSessionId is required");
        }
    }
}
