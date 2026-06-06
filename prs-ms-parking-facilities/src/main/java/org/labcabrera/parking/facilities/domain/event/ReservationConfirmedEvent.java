package org.labcabrera.parking.facilities.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationConfirmedEvent(
    UUID reservationId,
    UUID facilityId,
    String userId,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    LocalDateTime expiresAt,
    BigDecimal estimatedPrice,
    String currency) {

    public ReservationConfirmedEvent(UUID reservationId) {
        this(reservationId, null, null, null, null, null, null, null);
    }
}
