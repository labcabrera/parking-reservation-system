package org.labcabrera.parking.facilities.infrastructure.config;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "facilities.security.cors")
public record FacilitiesCorsProperties(
    List<String> allowedOrigins,
    List<String> allowedOriginPatterns) {

    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002",
        "http://localhost:3003",
        "http://localhost:5173",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",
        "http://127.0.0.1:3002",
        "http://127.0.0.1:3003",
        "http://127.0.0.1:5173");

    public FacilitiesCorsProperties {
        List<String> normalizedOrigins = normalize(allowedOrigins)
            .filter(origin -> !"*".equals(origin))
            .toList();
        List<String> wildcardOrigins = normalize(allowedOrigins)
            .filter("*"::equals)
            .toList();
        List<String> normalizedPatterns = Stream
            .concat(normalize(allowedOriginPatterns), wildcardOrigins.stream())
            .distinct()
            .toList();
        allowedOrigins = normalizedOrigins.isEmpty() ? DEFAULT_ALLOWED_ORIGINS : normalizedOrigins;
        allowedOriginPatterns = normalizedPatterns;
    }

    private static Stream<String> normalize(List<String> values) {
        return values == null
            ? Stream.empty()
            : values.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank());
    }
}
