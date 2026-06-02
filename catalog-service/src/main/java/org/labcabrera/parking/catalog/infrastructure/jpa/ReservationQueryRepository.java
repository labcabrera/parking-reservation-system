package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.util.UUID;

import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read-side handle on the {@link Reservation} aggregate table. The write side is
 * owned by Axon via {@code reservationRepository}; this repository is only used by
 * query handlers and REST controllers to project state.
 */
public interface ReservationQueryRepository extends JpaRepository<Reservation, UUID> {
}
