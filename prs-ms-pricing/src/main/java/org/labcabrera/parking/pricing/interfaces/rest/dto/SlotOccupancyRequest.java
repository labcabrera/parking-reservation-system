package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SlotOccupancy", description = "Current occupancy of one slot involved in a dynamic pricing calculation")
public record SlotOccupancyRequest(

    @NotNull
    @DecimalMin("0.0000")
    @DecimalMax("1.0000")
    @Schema(description = "Occupancy rate as a decimal ratio from 0.0000 to 1.0000", example = "0.7500")
    BigDecimal occupancyRate) {
}
