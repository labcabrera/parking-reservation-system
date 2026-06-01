package org.labcabrera.parking.reservation.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    /**
     * Per-IP: 5 requests per 60 seconds.
     * Per-session: 3 requests per 60 seconds.
     * Applied in HoldController via HoldRateLimitInterceptor.
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig byIpConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterConfig bySessionConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .limitForPeriod(3)
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
        registry.rateLimiter("hold-creation-by-ip", byIpConfig);
        registry.rateLimiter("hold-creation-by-session", bySessionConfig);
        return registry;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerConfig pricingConfig = CircuitBreakerConfig.from(defaultConfig)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .build();

        CircuitBreakerConfig paymentConfig = CircuitBreakerConfig.from(defaultConfig)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
        registry.circuitBreaker("pricingService", pricingConfig);
        registry.circuitBreaker("paymentService", paymentConfig);
        return registry;
    }
}
