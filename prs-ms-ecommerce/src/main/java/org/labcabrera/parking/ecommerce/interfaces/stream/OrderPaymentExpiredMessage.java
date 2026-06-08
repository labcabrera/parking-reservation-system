package org.labcabrera.parking.ecommerce.interfaces.stream;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentExpiredMessage(
    UUID orderId,
    UUID reservationId,
    LocalDateTime expiresAt,
    Instant occurredAt,
    String eventVersion) {
}
