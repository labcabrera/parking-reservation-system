package org.labcabrera.parking.catalog.infrastructure.jpa;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.config.ProcessingGroup;
import org.labcabrera.parking.catalog.domain.event.ReservationCancelledEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationConfirmedEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationExpiredEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationFailedEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationHeldEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationStartedEvent;
import org.labcabrera.parking.catalog.domain.valueobject.ReservationStatus;
import org.labcabrera.parking.catalog.infrastructure.jpa.entities.ReservationJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@ProcessingGroup("reservation-projection")
@RequiredArgsConstructor
class ReservationProjection {

    private final ReservationQueryRepository repository;

    @EventHandler
    @Transactional
    public void on(ReservationStartedEvent event) {
        ReservationJpaEntity entity = new ReservationJpaEntity();
        entity.setId(event.reservationId());
        entity.setFacilityId(event.facilityId());
        entity.setUserId(event.userId());
        entity.setCheckIn(event.checkIn());
        entity.setCheckOut(event.checkOut());
        entity.setStatus(ReservationStatus.PENDING);
        entity.setCreatedAt(java.time.LocalDateTime.now());
        entity.setExpiresAt(event.expiresAt());
        repository.save(entity);
    }

    @EventHandler
    @Transactional
    public void on(ReservationHeldEvent event) {
        repository.findById(event.reservationId()).ifPresent(entity -> {
            entity.setStatus(ReservationStatus.HELD);
            entity.setEstimatedPrice(event.estimatedPrice());
            entity.setCurrency(event.currency());
            repository.save(entity);
        });
    }

    @EventHandler
    @Transactional
    public void on(ReservationFailedEvent event) {
        updateStatus(event.reservationId(), ReservationStatus.FAILED, event.reason());
    }

    @EventHandler
    @Transactional
    public void on(ReservationConfirmedEvent event) {
        updateStatus(event.reservationId(), ReservationStatus.CONFIRMED, null);
    }

    @EventHandler
    @Transactional
    public void on(ReservationCancelledEvent event) {
        updateStatus(event.reservationId(), ReservationStatus.CANCELLED, event.reason());
    }

    @EventHandler
    @Transactional
    public void on(ReservationExpiredEvent event) {
        updateStatus(event.reservationId(), ReservationStatus.EXPIRED, null);
    }

    private void updateStatus(java.util.UUID reservationId, ReservationStatus status, String failureReason) {
        repository.findById(reservationId).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setFailureReason(failureReason);
            repository.save(entity);
        });
    }
}
