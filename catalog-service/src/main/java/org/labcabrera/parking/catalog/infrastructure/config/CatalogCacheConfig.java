package org.labcabrera.parking.catalog.infrastructure.config;

import java.time.Duration;

import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * Spring Cache configuration backed by Redis.
 * <p>
 * Uses {@link Jackson2JsonRedisSerializer} typed to {@link ParkingFacility} to
 * avoid adding {@code @class} type info in the JSON payload (which breaks with
 * {@code EnumSet} and other package-private JDK collections).
 * </p>
 * Cache name: {@code "parking-facilities"} — TTL controlled by
 * {@code catalog.cache.facility-ttl-seconds} (default 300 s / 5 min).
 */
@Configuration
@EnableCaching
public class CatalogCacheConfig {

    public static final String CACHE_FACILITIES = "parking-facilities";

    @Value("${catalog.cache.facility-ttl-seconds:300}")
    private long facilityTtlSeconds;

    @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
                Jackson2JsonRedisSerializer<ParkingFacility> valueSerializer = new Jackson2JsonRedisSerializer<>(ParkingFacility.class);
                valueSerializer.setObjectMapper(objectMapper);
                RedisCacheConfiguration facilityConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(facilityTtlSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(valueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(CACHE_FACILITIES, facilityConfig)
                .build();
    }
}
