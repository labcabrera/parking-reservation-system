package org.labcabrera.parking.reservation.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

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
