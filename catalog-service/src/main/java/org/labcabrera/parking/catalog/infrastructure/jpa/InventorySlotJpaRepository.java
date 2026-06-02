package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

    /**
     * Returns the peak occupancy per facility within [start, end) — used by the
     * availability search to compute the minimum free spots across the requested
     * range in a single round-trip. Facilities absent from the result have no
     * recorded slots in range and are fully available.
     * Each row: {@code [UUID facilityId, Integer maxReserved]}.
     */
    @Query("""
        SELECT s.facilityId, MAX(s.reserved)
          FROM InventorySlotJpaEntity s
         WHERE s.facilityId IN :facilityIds
           AND s.slotStart >= :start
           AND s.slotStart <  :end
         GROUP BY s.facilityId
        """)
    List<Object[]> findMaxReservedByFacility(
        @Param("facilityIds") Collection<UUID> facilityIds,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
