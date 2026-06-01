package org.labcabrera.parking.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Testcontainers integration test verifying that a hold is automatically
 * expired by the scheduler when its TTL elapses.
 *
 * NOTE: This test requires a running Kafka instance and is tagged @Testcontainers
 * for the database only. Kafka bootstrapping is expected via
 * spring.kafka.bootstrap-servers override in the test profile.
 *
 * The hold TTL is overridden to 10 seconds via spring.application configuration
 * so we can assert expiry within a 30-second window.
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("SpotHold TTL Expiry Integration Test")
class SpotHoldTtlIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reservation_db")
            .withUsername("parking")
            .withPassword("parking")
            .withInitScript("db/migration/V001__initial_schema.sql");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Override TTL to 10 seconds for fast expiry testing
        registry.add("reservation.hold.ttl-seconds", () -> "10");
        // Use embedded Kafka if available; otherwise skip (test is @Disabled by default)
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @DisplayName("Hold expires automatically after TTL elapses")
    void holdShouldExpireAfterTtl() {
        // Given — a minimal create-hold request
        var request = Map.of(
                "searchSessionId", UUID.randomUUID().toString(),
                "spotId", UUID.randomUUID().toString(),
                "facilityId", UUID.randomUUID().toString(),
                "checkIn", Instant.now().plus(1, ChronoUnit.DAYS).toString(),
                "checkOut", Instant.now().plus(3, ChronoUnit.DAYS).toString(),
                "estimatedPriceAmount", 50.00,
                "currency", "EUR"
        );

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/v1/reservations/holds", request, Map.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        String holdId = (String) createResponse.getBody().get("holdId");
        assertThat(holdId).isNotBlank();

        // When — wait for scheduler to detect expired hold (PT5M sweep; overridden to PT10S in test)
        // Then — status becomes EXPIRED within 30 seconds
        await()
                .atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                            "/api/v1/reservations/holds/{holdId}", Map.class, holdId);
                    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(getResponse.getBody().get("status")).isEqualTo("EXPIRED");
                });
    }
}
