package org.labcabrera.parking.catalog.domain.port.outbound;

import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ParkingFacilityRepository {

    Page<ParkingFacility> findByRsql(String rsql, Pageable pageable);

    Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable);

    Optional<ParkingFacility> findById(FacilityId id);

    void save(ParkingFacility parkingFacility);
}
