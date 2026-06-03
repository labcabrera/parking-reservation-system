package org.labcabrera.parking.catalog.interfaces.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.labcabrera.parking.catalog.domain.valueobject.InventorySlot;
import org.labcabrera.parking.catalog.interfaces.rest.dto.InventorySlotDto;

@Mapper(componentModel = "spring")
public interface InventorySlotMapper {

    @Mapping(target = "free", expression = "java(slot.free())")
    InventorySlotDto toDto(InventorySlot slot);
}
