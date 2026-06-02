package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationStatusDto(
    UUID id,
    UUID facilityId,
    String userId,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    String status,
    BigDecimal estimatedPrice,
    String currency,
    LocalDateTime expiresAt,
    String failureReason) {
}
