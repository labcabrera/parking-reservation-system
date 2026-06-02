package org.labcabrera.parking.catalog.infrastructure.jpa;

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
}
