package org.labcabrera.parking.facilities.interfaces.rest.mapper;

import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;
import org.labcabrera.parking.facilities.interfaces.rest.dto.ParkingFacilityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParkingFacilityMapper {

    @Mapping(target = "id", expression = "java(domain.getId().toString())")
    ParkingFacilityDto toDto(ParkingFacility domain);

    default String map(FacilityId id) {
        return id == null ? null : id.toString();
    }

}
