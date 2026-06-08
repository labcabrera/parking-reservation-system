package org.labcabrera.parking.facilities.infrastructure.config;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@ConfigurationProperties(prefix = "security.cors")
public record FacilitiesCorsProperties(
    List<String> allowedOrigins,
    List<String> allowedOriginPatterns,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    List<String> exposedHeaders,
    Boolean allowCredentials,
    Long maxAge) {

    private static final List<String> DEFAULT_ALLOWED_ORIGIN_PATTERNS = List.of("http://localhost:[*]");
    private static final List<String> DEFAULT_ALLOWED_METHODS = List.of(
        HttpMethod.DELETE.name(),
        HttpMethod.GET.name(),
        HttpMethod.OPTIONS.name(),
        HttpMethod.PATCH.name(),
        HttpMethod.POST.name(),
        HttpMethod.PUT.name());
    private static final List<String> DEFAULT_ALLOWED_HEADERS = List.of(
        HttpHeaders.ACCEPT,
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.CONTENT_TYPE,
        "X-Requested-With");
    private static final List<String> DEFAULT_EXPOSED_HEADERS = List.of(HttpHeaders.LOCATION);

    public FacilitiesCorsProperties {
        List<String> normalizedOrigins = normalize(allowedOrigins)
            .filter(origin -> !origin.contains("*"))
            .toList();
        List<String> wildcardOrigins = normalize(allowedOrigins)
            .filter(origin -> origin.contains("*"))
            .toList();
        List<String> normalizedPatterns = Stream
            .concat(normalize(allowedOriginPatterns), wildcardOrigins.stream())
            .distinct()
            .toList();

        allowedOrigins = normalizedOrigins;
        allowedOriginPatterns = normalizedPatterns.isEmpty() ? DEFAULT_ALLOWED_ORIGIN_PATTERNS : normalizedPatterns;
        allowedMethods = withDefault(allowedMethods, DEFAULT_ALLOWED_METHODS);
        allowedHeaders = withDefault(allowedHeaders, DEFAULT_ALLOWED_HEADERS);
        exposedHeaders = withDefault(exposedHeaders, DEFAULT_EXPOSED_HEADERS);
        allowCredentials = allowCredentials == null ? Boolean.TRUE : allowCredentials;
        maxAge = maxAge == null ? 3600L : maxAge;
    }

    private static List<String> withDefault(List<String> values, List<String> defaultValues) {
        List<String> normalized = normalize(values).toList();
        return normalized.isEmpty() ? defaultValues : normalized;
    }

    private static Stream<String> normalize(List<String> values) {
        return values == null
            ? Stream.empty()
            : values.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank());
    }
}
