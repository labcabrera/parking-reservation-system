package org.labcabrera.parking.catalog.domain.port.outbound;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.aggregate.SearchReservation;
import org.springframework.data.domain.Page;

public interface SearchReservationRepository {

    Optional<SearchReservation> findById(UUID id);

    Page<SearchReservation> findUserId(String userId);

    // Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable);

    // Optional<ParkingFacility> findById(FacilityId id);

    void save(SearchReservation searchReservation);
}
