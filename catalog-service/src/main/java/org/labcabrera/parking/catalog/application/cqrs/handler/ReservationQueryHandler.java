package org.labcabrera.parking.catalog.application.cqrs.handler;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.catalog.application.cqrs.query.FindReservationsQuery;
import org.labcabrera.parking.catalog.infrastructure.jpa.ReservationQueryRepository;
import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.labcabrera.parking.catalog.application.cqrs.query.GetReservationByIdQuery;

@Component
@RequiredArgsConstructor
public class ReservationQueryHandler {

    private final ReservationQueryRepository reservationRepository;

    @QueryHandler
    public Page<Reservation> handle(FindReservationsQuery q) {
        if (q.facilityId() != null) {
            return reservationRepository.findByFacilityOverlapping(q.facilityId(), q.start(), q.end(), q.pageable());
        }
        return reservationRepository.findOverlapping(q.start(), q.end(), q.pageable());
    }

    @QueryHandler
    public Reservation handle(GetReservationByIdQuery q) {
        return reservationRepository.findById(q.id()).orElse(null);
    }

}
