package org.labcabrera.parking.facilities.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.facilities.infrastructure.jpa.entities.ReservationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Read-side handle on the reservation projection table.
 */
public interface ReservationQueryRepository extends JpaRepository<ReservationJpaEntity, UUID> {

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findOverlapping(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.facilityId = :facilityId AND r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findByFacilityOverlapping(@Param("facilityId") UUID facilityId, @Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end, Pageable pageable);
}
