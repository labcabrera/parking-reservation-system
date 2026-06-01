package org.labcabrera.parking.catalog.infrastructure.redis;

import org.labcabrera.parking.catalog.domain.model.AvailabilityWindow;
import org.labcabrera.parking.catalog.domain.model.FacilityId;
import org.labcabrera.parking.catalog.domain.port.outbound.AvailabilityCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class RedisAvailabilityCache implements AvailabilityCache {

    private static final Duration TTL = Duration.ofSeconds(60);
    private static final String KEY_PREFIX = "availability:";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisAvailabilityCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Integer> getAvailableSpotCount(FacilityId facilityId, AvailabilityWindow window) {
        String value = redisTemplate.opsForValue().get(buildKey(facilityId, window));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(value));
    }

    @Override
    public void putAvailableSpotCount(FacilityId facilityId, AvailabilityWindow window, int count) {
        redisTemplate.opsForValue().set(buildKey(facilityId, window), String.valueOf(count), TTL);
    }

    @Override
    public void invalidate(String facilityId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + facilityId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String buildKey(FacilityId facilityId, AvailabilityWindow window) {
        return KEY_PREFIX + facilityId + ":" + window.checkIn() + ":" + window.checkOut();
    }
}
