package org.labcabrera.parking.facilities.domain.port;

import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ParkingFacilityRepository {

    Page<ParkingFacility> findByRsql(String rsql, Pageable pageable);

    Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable);

    Optional<ParkingFacility> findById(FacilityId id);

    boolean existsByName(String name);

    void save(ParkingFacility parkingFacility);
}
