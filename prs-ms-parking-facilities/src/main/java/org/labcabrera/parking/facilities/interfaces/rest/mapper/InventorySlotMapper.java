package org.labcabrera.parking.facilities.interfaces.rest.mapper;

import org.labcabrera.parking.facilities.domain.valueobject.InventorySlot;
import org.labcabrera.parking.facilities.interfaces.rest.dto.InventorySlotDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventorySlotMapper {

    @Mapping(target = "free", expression = "java(slot.free())")
    InventorySlotDto toDto(InventorySlot slot);
}
