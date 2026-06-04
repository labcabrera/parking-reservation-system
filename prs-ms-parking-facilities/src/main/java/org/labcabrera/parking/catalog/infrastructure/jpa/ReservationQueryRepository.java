package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Read-side handle on the {@link Reservation} aggregate table. The write side is
 * owned by Axon via {@code reservationRepository}; this repository is only used by
 * query handlers and REST controllers to project state.
 */
public interface ReservationQueryRepository extends JpaRepository<Reservation, UUID> {

	@Query("SELECT r FROM Reservation r WHERE r.checkIn < :end AND r.checkOut > :start")
	Page<Reservation> findOverlapping(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT r FROM Reservation r WHERE r.facilityId = :facilityId AND r.checkIn < :end AND r.checkOut > :start")
	Page<Reservation> findByFacilityOverlapping(@Param("facilityId") UUID facilityId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
