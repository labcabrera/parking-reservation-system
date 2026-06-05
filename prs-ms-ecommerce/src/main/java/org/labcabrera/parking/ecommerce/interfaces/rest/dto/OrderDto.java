package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Order", description = "Ecommerce order")
public record OrderDto(

    @Schema(description = "Order identifier")
    UUID id,

    @Schema(description = "Hold identifier")
    UUID holdId,

    @Schema(description = "Order expiration timestamp")
    LocalDateTime expiresAt,

    @Schema(description = "Order amount")
    BigDecimal amount,

    @Schema(description = "ISO 4217 currency code")
    String currency,

    @Schema(description = "Current order status")
    OrderStatus status,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last payment attempt identifier")
    UUID lastPaymentAttemptId,

    @Schema(description = "Failure reason when payment failed")
    String failureReason) {
}
