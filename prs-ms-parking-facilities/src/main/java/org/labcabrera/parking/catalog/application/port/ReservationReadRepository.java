package org.labcabrera.parking.catalog.application.port;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationReadRepository {

    Optional<Reservation> findById(UUID id);

    Page<Reservation> findOverlapping(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByFacilityOverlapping(UUID facilityId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
