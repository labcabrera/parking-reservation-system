package org.labcabrera.parking.facilities.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationPaymentExpiredEvent(
    UUID reservationId,
    UUID orderId,
    UUID facilityId,
    LocalDateTime checkIn,
    LocalDateTime checkOut) {
}
