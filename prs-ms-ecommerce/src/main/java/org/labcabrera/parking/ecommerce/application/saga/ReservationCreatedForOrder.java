package org.labcabrera.parking.ecommerce.application.saga;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCreatedForOrder(
    UUID reservationId,
    String bookingSessionId,
    LocalDateTime expiresAt,
    BigDecimal amount,
    String currency) {
}
