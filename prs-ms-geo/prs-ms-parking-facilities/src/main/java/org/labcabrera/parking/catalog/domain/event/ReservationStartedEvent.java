package org.labcabrera.parking.catalog.domain.event;

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
