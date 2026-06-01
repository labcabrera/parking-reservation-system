package org.labcabrera.parking.catalog.domain.port.outbound;

import org.labcabrera.parking.catalog.domain.model.FacilityId;
import org.labcabrera.parking.catalog.domain.model.FacilityStatus;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FacilityRepository {

    Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable);

    Optional<ParkingFacility> findById(FacilityId id);
}
