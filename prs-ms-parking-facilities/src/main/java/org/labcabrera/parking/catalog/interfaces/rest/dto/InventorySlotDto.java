package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InventorySlot", description = "Inventory slot information for a facility and time slot")
public record InventorySlotDto(

    @Schema(description = "Slot start (inclusive)")
    LocalDateTime slotStart,
    
    @Schema(description = "Capacity of the slot")
    int capacity,
    
    @Schema(description = "Currently reserved units")
    int reserved,
    
    @Schema(description = "Free units (capacity - reserved)")
    int free
) {}
