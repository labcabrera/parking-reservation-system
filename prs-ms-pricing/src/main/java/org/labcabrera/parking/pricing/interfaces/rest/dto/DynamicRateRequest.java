package org.labcabrera.parking.pricing.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DynamicRateRequest", description = "Request to calculate a dynamic price from a pricing rule and slot occupancy")
public record DynamicRateRequest(

    @NotNull
    @Schema(description = "Pricing rule identifier used to resolve hourly or daily rate parameters", example = "018f4f3b-9c52-7d87-9f0b-7b2d8a4b8c12")
    UUID pricingRuleId,

    @NotNull
    @Schema(description = "Slot duration model. SHORT represents a half-hour slot; LONG represents a one-day slot.", example = "SHORT")
    PricingSlotType slotType,

    @NotEmpty
    @Schema(description = "Involved slots with their current occupancy rates")
    List<@Valid SlotOccupancyRequest> slots) {
}
