package org.labcabrera.parking.facilities.interfaces.rest.dto;

import java.time.LocalDateTime;

import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InventorySlot", description = "Inventory slot information for a facility and time slot")
public record InventorySlotDto(

    @Schema(description = "Slot start (inclusive)") LocalDateTime slotStart,

    @Schema(description = "Inventory bucket type") InventoryBlockType blockType,

    @Schema(description = "Capacity of the slot") int capacity,

    @Schema(description = "Currently reserved units") int reserved,

    @Schema(description = "Free units (capacity - reserved)") int free) {
}
