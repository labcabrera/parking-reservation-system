package org.labcabrera.parking.catalog.interfaces.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ReservationDto;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "status", expression = "java(r.getStatus() != null ? r.getStatus().name() : null)")
    ReservationDto toDto(Reservation r);
}
