package org.labcabrera.parking.ecommerce.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    UUID holdId,
    LocalDateTime expiresAt,
    BigDecimal amount,
    String currency,
    LocalDateTime occurredAt) {
}
