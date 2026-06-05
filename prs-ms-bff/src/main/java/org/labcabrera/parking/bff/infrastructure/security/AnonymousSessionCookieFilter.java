package org.labcabrera.parking.bff.infrastructure.security;

import java.io.IOException;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AnonymousSessionCookieFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "bearer ";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (shouldCreateAnonymousSession(request)) {
            request.getSession(true);
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldCreateAnonymousSession(HttpServletRequest request) {
        return !isActuatorRequest(request)
            && !isPreflightRequest(request)
            && !hasBearerToken(request);
    }

    private boolean hasBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null
            && authorization.toLowerCase(Locale.ROOT).startsWith(BEARER_PREFIX);
    }

    private boolean isActuatorRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
