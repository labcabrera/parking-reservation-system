package org.labcabrera.parking.pricing.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.cors")
public record PricingCorsProperties(
    List<String> allowedOrigins,
    List<String> allowedOriginPatterns,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    List<String> exposedHeaders,
    Boolean allowCredentials,
    Long maxAge) {
}
