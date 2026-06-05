package org.labcabrera.parking.facilities.infrastructure.jpa;

import org.labcabrera.parking.facilities.domain.aggregate.Reservation;
import org.labcabrera.parking.facilities.infrastructure.jpa.entities.ReservationJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationJpaMapper {

    default Reservation toDomain(ReservationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Reservation(
            entity.getId(),
            entity.getFacilityId(),
            entity.getUserId(),
            entity.getCheckIn(),
            entity.getCheckOut(),
            entity.getStatus(),
            entity.getEstimatedPrice(),
            entity.getCurrency(),
            entity.getCreatedAt(),
            entity.getExpiresAt(),
            entity.getFailureReason(),
            entity.getVersion());
    }
}
