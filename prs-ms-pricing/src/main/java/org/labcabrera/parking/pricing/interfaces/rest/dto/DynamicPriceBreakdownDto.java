package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DynamicPriceBreakdown", description = "Monetary breakdown for a dynamic pricing calculation")
public record DynamicPriceBreakdownDto(
    @Schema(description = "Base amount before taxes", example = "35.0000")
    BigDecimal baseAmount,
    @Schema(description = "Tax amount calculated from the pricing rule tax rate", example = "7.3500")
    BigDecimal taxAmount,
    @Schema(description = "Total amount including taxes", example = "42.3500")
    BigDecimal totalAmount,
    @Schema(description = "Currency resolved from the pricing rule rate", example = "EUR")
    String currency) {
}
