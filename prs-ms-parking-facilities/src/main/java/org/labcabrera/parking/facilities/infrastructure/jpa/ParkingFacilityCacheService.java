package org.labcabrera.parking.facilities.infrastructure.jpa;

import static org.labcabrera.parking.facilities.infrastructure.config.CatalogCacheConfig.CACHE_FACILITIES;

import java.util.UUID;

import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Cache layer for {@link ParkingFacility} look-ups. Package-private; consumed only by
 * {@link FacilityRepositoryAdapter}.
 *
 * <p>
 * The methods return {@code ParkingFacility} (nullable) rather than
 * {@code Optional<ParkingFacility>} because Spring Cache stores the raw return value —
 * serializing an {@code Optional} wrapper over Redis would require additional Jackson
 * configuration.
 * </p>
 *
 * <p>
 * Cache key: facility UUID string → Redis key {@code parking-facilities::<uuid>}.
 * </p>
 */
@Component
class ParkingFacilityCacheService {

    private final FacilityJpaRepository jpaRepository;
    private final FacilityJpaMapper mapper;

    ParkingFacilityCacheService(FacilityJpaRepository jpaRepository, FacilityJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * Returns the facility from the cache, or queries the DB on a miss and populates the
     * cache. Returns {@code null} when the facility does not exist (null results are
     * never stored thanks to {@code disableCachingNullValues}).
     */
    @Cacheable(cacheNames = CACHE_FACILITIES, key = "#id.toString()", unless = "#result == null")
    public ParkingFacility findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain).orElse(null);
    }

    /**
     * Evicts the facility entry from the cache. Called before every write so that
     * subsequent reads hit the DB and repopulate with fresh data.
     */
    @CacheEvict(cacheNames = CACHE_FACILITIES, key = "#id.toString()")
    public void evict(UUID id) {
        // eviction handled by Spring Cache AOP
    }
}
