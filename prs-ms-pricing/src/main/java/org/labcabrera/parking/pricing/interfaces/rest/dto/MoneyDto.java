package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Money", description = "Monetary amount with ISO-4217 currency")
public record MoneyDto(

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "Monetary amount", example = "12.50")
    BigDecimal amount,

    @NotBlank
    @Schema(description = "ISO-4217 currency code", example = "EUR")
    String currency) {
}
