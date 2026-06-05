package org.labcabrera.parking.facilities.interfaces.rest.mapper;

import org.labcabrera.parking.facilities.application.cqrs.command.CreateParkingFacilityCommand;
import org.labcabrera.parking.facilities.interfaces.rest.dto.CreateParkingFacilityRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateParkingFacilityMapper {

    CreateParkingFacilityCommand toCommand(CreateParkingFacilityRequest request);

}
