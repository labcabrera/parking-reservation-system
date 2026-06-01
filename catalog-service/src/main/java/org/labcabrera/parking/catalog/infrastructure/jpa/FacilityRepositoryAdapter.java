package org.labcabrera.parking.catalog.infrastructure.jpa;

import org.labcabrera.parking.catalog.domain.model.FacilityId;
import org.labcabrera.parking.catalog.domain.model.FacilityStatus;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.labcabrera.parking.catalog.domain.port.outbound.FacilityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class FacilityRepositoryAdapter implements FacilityRepository {

    private final FacilityJpaRepository jpaRepository;

    public FacilityRepositoryAdapter(FacilityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable) {
        return jpaRepository
                .findByTextAndStatus(text, status.name(), pageable)
                .map(FacilityJpaMapper::toDomain);
    }

    @Override
    public Optional<ParkingFacility> findById(FacilityId id) {
        return jpaRepository.findById(id.value()).map(FacilityJpaMapper::toDomain);
    }
}
