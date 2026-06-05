package org.labcabrera.parking.bff.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;

class AnonymousSessionCookieFilterTest {

    private final AnonymousSessionCookieFilter filter = new AnonymousSessionCookieFilter();

    @Test
    void createsSessionForAnonymousNavigation() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/parking-facilities/availability");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getSession(false)).isNotNull();
    }

    @Test
    void doesNotCreateAnonymousSessionWhenBearerTokenIsPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/parking-facilities/availability");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void doesNotCreateSessionForActuatorRequests() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getSession(false)).isNull();
    }
}
