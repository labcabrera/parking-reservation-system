package org.labcabrera.parking.catalog.interfaces.rest.mapper;

import org.mapstruct.Mapper;

import org.labcabrera.parking.catalog.interfaces.rest.dto.CreateParkingFacilityRequest;
import org.labcabrera.parking.catalog.application.cqrs.command.CreateParkingFacilityCommand;

@Mapper(componentModel = "spring")
public interface CreateParkingFacilityMapper {

    CreateParkingFacilityCommand toCommand(CreateParkingFacilityRequest request);

}
