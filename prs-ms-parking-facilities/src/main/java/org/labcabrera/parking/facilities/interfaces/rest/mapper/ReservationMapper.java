package org.labcabrera.parking.facilities.interfaces.rest.mapper;

import org.labcabrera.parking.facilities.domain.aggregate.Reservation;
import org.labcabrera.parking.facilities.interfaces.rest.dto.ReservationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "status", expression = "java(r.getStatus() != null ? r.getStatus().name() : null)")
    ReservationDto toDto(Reservation r);
}
