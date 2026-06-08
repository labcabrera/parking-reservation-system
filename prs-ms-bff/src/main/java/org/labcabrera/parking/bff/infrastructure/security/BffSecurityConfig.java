package org.labcabrera.parking.bff.infrastructure.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class BffSecurityConfig {

    private static final String UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private final List<String> allowedOrigins;

    public BffSecurityConfig(
        @Value("${bff.security.cors.allowed-origins:http://localhost:3000,http://localhost:3001}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicCheckoutSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new OrRequestMatcher(publicCheckoutMatchers()))
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**")
                .permitAll()
                .requestMatchers(publicCheckoutMatchersArray())
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/checkout/reservations")
                .authenticated()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .addFilterAfter(new AnonymousSessionCookieFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    private List<RequestMatcher> publicCheckoutMatchers() {
        return List.of(
            pathMatcher(HttpMethod.OPTIONS, "^/.*$"),
            pathMatcher(HttpMethod.GET, "^/api/v1/checkout/search$"),
            pathMatcher(HttpMethod.POST, "^/api/v1/checkout/select-option$"),
            pathMatcher(HttpMethod.POST, "^/api/v1/checkout/" + UUID_PATTERN + "/select-option$"),
            pathMatcher(HttpMethod.GET, "^/api/v1/checkout/" + UUID_PATTERN + "$"),
            pathMatcher(HttpMethod.POST, "^/api/v1/checkout/" + UUID_PATTERN + "/confirm$"),
            pathMatcher(HttpMethod.POST, "^/api/v1/checkout/" + UUID_PATTERN + "/payment-attempts$"),
            pathMatcher(HttpMethod.GET, "^/api/v1/payment-methods$"));
    }

    private RequestMatcher[] publicCheckoutMatchersArray() {
        return publicCheckoutMatchers().toArray(RequestMatcher[]::new);
    }

    private RequestMatcher pathMatcher(HttpMethod method, String pathRegex) {
        return request -> method.matches(request.getMethod())
            && pathWithinApplication(request).matches(pathRegex);
    }

    private String pathWithinApplication(jakarta.servlet.http.HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of(
            HttpMethod.DELETE.name(),
            HttpMethod.GET.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name()));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
