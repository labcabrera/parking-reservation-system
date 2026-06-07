package org.labcabrera.parking.facilities.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.valueobject.ReservationStatus;
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

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.userId = :userId AND r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findByUserOverlapping(@Param("userId") String userId, @Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.bookingSessionId = :bookingSessionId AND r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findByBookingSessionOverlapping(@Param("bookingSessionId") String bookingSessionId,
		@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.facilityId = :facilityId AND r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findByFacilityOverlapping(@Param("facilityId") UUID facilityId, @Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.userId = :userId AND r.facilityId = :facilityId AND r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findByUserAndFacilityOverlapping(
		@Param("userId") String userId,
		@Param("facilityId") UUID facilityId,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end,
		Pageable pageable);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.bookingSessionId = :bookingSessionId AND r.facilityId = :facilityId AND r.checkIn < :end AND r.checkOut > :start")
	Page<ReservationJpaEntity> findByBookingSessionAndFacilityOverlapping(
		@Param("bookingSessionId") String bookingSessionId,
		@Param("facilityId") UUID facilityId,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end,
		Pageable pageable);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.userId = :userId AND r.status = :status AND r.id <> :excludedReservationId")
	List<ReservationJpaEntity> findHeldByUserExcluding(@Param("userId") String userId,
		@Param("status") ReservationStatus status, @Param("excludedReservationId") UUID excludedReservationId);

	@Query("SELECT r FROM ReservationJpaEntity r WHERE r.bookingSessionId = :bookingSessionId AND r.status = :status AND r.id <> :excludedReservationId")
	List<ReservationJpaEntity> findHeldByBookingSessionExcluding(@Param("bookingSessionId") String bookingSessionId,
		@Param("status") ReservationStatus status, @Param("excludedReservationId") UUID excludedReservationId);
}
