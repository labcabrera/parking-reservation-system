package org.labcabrera.parking.bff.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Current state of a user's checkout session. The {@code checkoutId} uniquely identifies
 * this checkout and is used in all subsequent operations.
 */
public record CheckoutDto(

    /** Unique identifier for this checkout session (= reservation / hold ID). */
    UUID checkoutId,

    /** Lifecycle status of the hold: PENDING, CONFIRMED, CANCELLED, EXPIRED. */
    String status,

    UUID facilityId,

    String facilityName,

    LocalDateTime checkIn,

    LocalDateTime checkOut,

    /** Hold expiry time. The user must confirm before this instant. */
    LocalDateTime expiresAt,

    BigDecimal amount,

    String currency,

    /** Payment status when an order exists: PENDING_PAYMENT, PAID, FAILED. */
    String paymentStatus,

    /**
     * Redirect URL provided by the payment gateway after a successful payment attempt.
     */
    String redirectUrl) {
}
