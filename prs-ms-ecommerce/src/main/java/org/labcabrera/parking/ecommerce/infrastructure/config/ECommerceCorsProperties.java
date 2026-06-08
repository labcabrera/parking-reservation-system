package org.labcabrera.parking.ecommerce.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.cors")
public record ECommerceCorsProperties(
    List<String> allowedOrigins,
    List<String> allowedOriginPatterns,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    List<String> exposedHeaders,
    Boolean allowCredentials,
    Long maxAge) {
}
