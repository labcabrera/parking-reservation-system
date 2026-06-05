package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MoneyDto(

    @NotNull
    @DecimalMin("0.00")
    BigDecimal amount,

    @NotBlank
    String currency) {
}
