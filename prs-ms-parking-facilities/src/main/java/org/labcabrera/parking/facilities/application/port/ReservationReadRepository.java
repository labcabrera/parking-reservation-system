package org.labcabrera.parking.facilities.application.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.aggregate.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationReadRepository {

    Optional<Reservation> findById(UUID id);

    Page<Reservation> findOverlapping(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByUserOverlapping(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByBookingSessionOverlapping(String bookingSessionId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByFacilityOverlapping(UUID facilityId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByUserAndFacilityOverlapping(String userId, UUID facilityId, LocalDateTime start, LocalDateTime end,
        Pageable pageable);

    Page<Reservation> findByBookingSessionAndFacilityOverlapping(String bookingSessionId, UUID facilityId, LocalDateTime start,
        LocalDateTime end, Pageable pageable);

    List<Reservation> findHeldByUserExcluding(String userId, UUID excludedReservationId);

    List<Reservation> findHeldByBookingSessionExcluding(String bookingSessionId, UUID excludedReservationId);
}
