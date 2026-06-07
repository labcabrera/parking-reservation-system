package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateOrderRequest", description = "Request payload to create an ecommerce order from a parking hold")
public record CreateOrderRequest(

    @NotNull
    @Schema(description = "Hold identifier confirmed by the reservation flow", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID holdId,

    @Schema(description = "Stable booking session id used to correlate checkout calls")
    String bookingSessionId,

    @NotNull
    @Future
    @Schema(description = "Order expiration timestamp", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime expiresAt,

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "Order amount", examples = "37.50", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal amount,

    @NotBlank
    @Schema(description = "ISO 4217 currency code", examples = "EUR", requiredMode = Schema.RequiredMode.REQUIRED)
    String currency) {
}
