package org.labcabrera.parking.facilities.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.facilities.application.port.ReservationReadRepository;
import org.labcabrera.parking.facilities.domain.aggregate.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ReservationReadRepositoryAdapter implements ReservationReadRepository {

    private final ReservationQueryRepository repository;
    private final ReservationJpaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findOverlapping(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return repository.findOverlapping(start, end, pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findByFacilityOverlapping(UUID facilityId, LocalDateTime start, LocalDateTime end,
        Pageable pageable) {
        return repository.findByFacilityOverlapping(facilityId, start, end, pageable).map(mapper::toDomain);
    }
}
