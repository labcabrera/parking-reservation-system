package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventorySlotJpaRepository extends JpaRepository<InventorySlotJpaEntity, UUID> {

    Optional<InventorySlotJpaEntity> findByFacilityIdAndSlotStart(UUID facilityId, LocalDateTime slotStart);

    /**
     * Optimistic atomic hold: increments {@code reserved} only when capacity remains
     * AND the row hasn't changed since we read it. Returns the number of affected rows
     * (1 = success, 0 = stale version or no capacity).
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE InventorySlotJpaEntity s
           SET s.reserved = s.reserved + 1,
               s.version  = s.version + 1
         WHERE s.id = :id
           AND s.version = :version
           AND s.reserved < s.capacity
        """)
    int tryHold(@Param("id") UUID id, @Param("version") long version);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE InventorySlotJpaEntity s
           SET s.reserved = s.reserved - 1,
               s.version  = s.version + 1
         WHERE s.id = :id
           AND s.reserved > 0
        """)
    int release(@Param("id") UUID id);
}
