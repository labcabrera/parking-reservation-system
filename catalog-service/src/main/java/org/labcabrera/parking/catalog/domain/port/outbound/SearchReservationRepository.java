package org.labcabrera.parking.catalog.domain.port.outbound;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.springframework.data.domain.Page;

public interface SearchReservationRepository {

    Optional<Reservation> findById(UUID id);

    Page<Reservation> findUserId(String userId);

    // Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable);

    // Optional<ParkingFacility> findById(FacilityId id);

    void save(Reservation searchReservation);
}
