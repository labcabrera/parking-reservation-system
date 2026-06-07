package org.labcabrera.parking.bff.infrastructure.security;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(classes = {
    BffSecurityConfig.class,
    BffSecurityConfigTest.SecurityTestController.class,
    BffSecurityConfigTest.JwtDecoderTestConfig.class,
    BffSecurityConfigTest.WebMvcTestConfig.class
})
class BffSecurityConfigTest {

    private final WebApplicationContext context;
    private MockMvc mockMvc;

    BffSecurityConfigTest(WebApplicationContext context) {
        this.context = context;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void checkoutSearchIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/checkout/search"))
            .andExpect(status().isOk());
    }

    @Test
    void paymentMethodsArePublic() throws Exception {
        mockMvc.perform(get("/api/v1/payment-methods"))
            .andExpect(status().isOk());
    }

    @Test
    void checkoutMutationIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/select-option"))
            .andExpect(status().isOk());
    }

    @Test
    void deprecatedCheckoutMutationPathIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/{checkoutId}/select-option", UUID.randomUUID()))
            .andExpect(status().isOk());
    }

    @Test
    void checkoutReadIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/checkout/{checkoutId}", UUID.randomUUID()))
            .andExpect(status().isOk());
    }

    @Test
    void userReservationsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/checkout/reservations"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanAccessProtectedCheckoutEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/checkout/{checkoutId}", UUID.randomUUID()).with(jwt()))
            .andExpect(status().isOk());
    }

    @Test
    void corsPreflightIsPublic() throws Exception {
        mockMvc.perform(options("/api/v1/checkout/{checkoutId}/confirm", UUID.randomUUID())
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk());
    }

    @RestController
    @RequestMapping("/api/v1")
    static class SecurityTestController {

        @GetMapping("/checkout/search")
        String search() {
            return "search";
        }

        @GetMapping("/payment-methods")
        String paymentMethods() {
            return "payment-methods";
        }

        @PostMapping("/checkout/select-option")
        String selectOption() {
            return "select-option";
        }

        @PostMapping("/checkout/{checkoutId}/select-option")
        String selectOptionWithClientId(@PathVariable UUID checkoutId) {
            return checkoutId.toString();
        }

        @PostMapping("/checkout/{checkoutId}/confirm")
        String confirm(@PathVariable UUID checkoutId) {
            return checkoutId.toString();
        }

        @GetMapping("/checkout/{checkoutId}")
        String checkout(@PathVariable UUID checkoutId) {
            return checkoutId.toString();
        }

        @GetMapping("/checkout/reservations")
        String reservations() {
            return "reservations";
        }
    }

    @Configuration
    @EnableWebMvc
    static class WebMvcTestConfig {
    }

    @Configuration
    static class JwtDecoderTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }
}
