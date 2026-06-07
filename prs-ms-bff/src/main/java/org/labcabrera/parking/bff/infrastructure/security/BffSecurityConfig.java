package org.labcabrera.parking.bff.infrastructure.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class BffSecurityConfig {

    private final List<String> allowedOrigins;

    public BffSecurityConfig(
        @Value("${bff.security.cors.allowed-origins:http://localhost:3000,http://localhost:3001}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/checkout/search")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/checkout/select-option")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/checkout/reservations")
                .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/checkout/{checkoutId}")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/checkout/{checkoutId}/confirm")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/checkout/{checkoutId}/payment-attempts")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/payment-methods")
                .permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .addFilterAfter(new AnonymousSessionCookieFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
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
