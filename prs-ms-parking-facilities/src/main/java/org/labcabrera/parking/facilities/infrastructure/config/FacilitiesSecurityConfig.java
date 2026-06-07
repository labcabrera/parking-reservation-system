package org.labcabrera.parking.facilities.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(FacilitiesCorsProperties.class)
public class FacilitiesSecurityConfig {

        private final FacilitiesCorsProperties corsProperties;

        public FacilitiesSecurityConfig(FacilitiesCorsProperties corsProperties) {
                this.corsProperties = corsProperties;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
                http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource))
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll())
                        .csrf(csrf -> csrf.disable());
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(corsProperties.allowedOrigins());
                configuration.setAllowedOriginPatterns(corsProperties.allowedOriginPatterns());
                configuration.setAllowedMethods(List.of(
                        HttpMethod.DELETE.name(),
                        HttpMethod.GET.name(),
                        HttpMethod.OPTIONS.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name()));
                configuration.setAllowedHeaders(List.of(
                        HttpHeaders.ACCEPT,
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        "X-Requested-With"));
                configuration.setExposedHeaders(List.of(HttpHeaders.LOCATION));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
