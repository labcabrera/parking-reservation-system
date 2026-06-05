package org.labcabrera.parking.facilities.infrastructure.jpa;

import org.labcabrera.parking.facilities.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.facilities.infrastructure.jpa.entities.ParkingFacilityJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

@Repository

public interface FacilityJpaRepository
        extends JpaRepository<ParkingFacilityJpaEntity, UUID>, JpaSpecificationExecutor<ParkingFacilityJpaEntity> {

        @Query("""
                SELECT f FROM ParkingFacilityJpaEntity f
                WHERE f.status = :status
                AND (:text = '' OR LOWER(f.name) LIKE LOWER(CONCAT('%', :text, '%'))
                               OR LOWER(f.city) LIKE LOWER(CONCAT('%', :text, '%')))
                """)
        Page<ParkingFacilityJpaEntity> findByTextAndStatus(
                @Param("text") String text,
                @Param("status") String status,
                Pageable pageable);

        /**
         * Free-text candidate lookup used by the availability search. Matches the
         * supplied text fragment (case-insensitive) against name, city or address.
         * Caller-supplied {@link Pageable} limits the candidate set size.
         */
        @Query("""
                SELECT f FROM ParkingFacilityJpaEntity f
                WHERE f.status = :status
                  AND (LOWER(f.name)    LIKE LOWER(CONCAT('%', :text, '%'))
                    OR LOWER(f.city)    LIKE LOWER(CONCAT('%', :text, '%'))
                    OR LOWER(f.address) LIKE LOWER(CONCAT('%', :text, '%')))
                """)
        Page<ParkingFacilityJpaEntity> searchCandidates(
                @Param("text") String text,
                @Param("status") FacilityStatus status,
                Pageable pageable);

        boolean existsByName(String name);
}
