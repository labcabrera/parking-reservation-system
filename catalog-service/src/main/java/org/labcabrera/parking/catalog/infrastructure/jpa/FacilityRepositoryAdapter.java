package org.labcabrera.parking.catalog.infrastructure.jpa;

import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
@Slf4j
public class FacilityRepositoryAdapter implements ParkingFacilityRepository {

    private final FacilityJpaRepository jpaRepository;
    private final FacilityJpaMapper mapper;

    public FacilityRepositoryAdapter(FacilityJpaRepository jpaRepository, FacilityJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable) {
        return jpaRepository
            .findByTextAndStatus(text, status.name(), pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<ParkingFacility> findById(FacilityId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void save(ParkingFacility parkingFacility) {
        if (parkingFacility == null) {
            return;
        }
        UUID id = parkingFacility.getId() != null ? parkingFacility.getId().value() : null;
        if (id != null && jpaRepository.existsById(id)) {
            log.info("Updating parking facility with id: {}", id);
            ParkingFacilityJpaEntity entity = jpaRepository.findById(id).orElseGet(() -> mapper.toEntity(parkingFacility));
            entity.updateFrom(parkingFacility);
            jpaRepository.save(entity);
        }
        else {
            log.info("Creating new parking facility with id: {}", id);
            ParkingFacilityJpaEntity entity = mapper.toEntity(parkingFacility);
            jpaRepository.save(entity);
        }
    }

    @Override
    public Page<ParkingFacility> findByRsql(String rsql, Pageable pageable) {
        if (rsql == null || rsql.isBlank()) {
            return jpaRepository.findAll(pageable).map(mapper::toDomain);
        }
        var spec = RsqlSpecificationBuilder.build(rsql);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }
}
