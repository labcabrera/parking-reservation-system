package org.labcabrera.parking.facilities.infrastructure.stream;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCreatedMessage(
    UUID reservationId,
    UUID facilityId,
    String userId,
    String bookingSessionId,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    LocalDateTime expiresAt,
    BigDecimal amount,
    String currency,
    Instant occurredAt,
    String eventVersion) {
}
