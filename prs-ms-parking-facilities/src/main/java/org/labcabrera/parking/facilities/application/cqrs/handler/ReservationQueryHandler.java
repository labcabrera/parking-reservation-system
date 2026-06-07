package org.labcabrera.parking.facilities.application.cqrs.handler;

import java.util.List;
import java.util.Optional;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.facilities.application.cqrs.query.FindHeldReservationsForOwnerQuery;
import org.labcabrera.parking.facilities.application.cqrs.query.FindReservationsQuery;
import org.labcabrera.parking.facilities.application.cqrs.query.GetReservationByIdQuery;
import org.labcabrera.parking.facilities.application.port.ReservationReadRepository;
import org.labcabrera.parking.facilities.domain.aggregate.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationQueryHandler {

    private final ReservationReadRepository reservationRepository;

    // Axon cant handle properly generic types, so we need to cast the response type in the controller
    @SuppressWarnings("rawtypes")
    @QueryHandler
    public Page handle(FindReservationsQuery q) {
        if (q.userId() != null && !q.userId().isBlank() && q.facilityId() != null) {
            return reservationRepository.findByUserAndFacilityOverlapping(q.userId(), q.facilityId(), q.start(), q.end(), q.pageable());
        }
        if (q.userId() != null && !q.userId().isBlank()) {
            return reservationRepository.findByUserOverlapping(q.userId(), q.start(), q.end(), q.pageable());
        }
        if (q.bookingSessionId() != null && !q.bookingSessionId().isBlank() && q.facilityId() != null) {
            return reservationRepository.findByBookingSessionAndFacilityOverlapping(q.bookingSessionId(), q.facilityId(), q.start(),
                q.end(), q.pageable());
        }
        if (q.bookingSessionId() != null && !q.bookingSessionId().isBlank()) {
            return reservationRepository.findByBookingSessionOverlapping(q.bookingSessionId(), q.start(), q.end(), q.pageable());
        }
        if (q.facilityId() != null) {
            return reservationRepository.findByFacilityOverlapping(q.facilityId(), q.start(), q.end(), q.pageable());
        }
        return reservationRepository.findOverlapping(q.start(), q.end(), q.pageable());
    }

    @QueryHandler
    public List<Reservation> handle(FindHeldReservationsForOwnerQuery q) {
        if (q.userId() != null && !q.userId().isBlank()) {
            return reservationRepository.findHeldByUserExcluding(q.userId(), q.excludedReservationId());
        }
        return reservationRepository.findHeldByBookingSessionExcluding(q.bookingSessionId(), q.excludedReservationId());
    }

    @QueryHandler
    public Optional<Reservation> handle(GetReservationByIdQuery q) {
        return reservationRepository.findById(q.id());
    }

}
