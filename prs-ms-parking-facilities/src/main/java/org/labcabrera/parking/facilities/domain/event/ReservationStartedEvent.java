package org.labcabrera.parking.facilities.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationStartedEvent(
    UUID reservationId,
    UUID facilityId,
    String userId,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    LocalDateTime expiresAt) {
}
