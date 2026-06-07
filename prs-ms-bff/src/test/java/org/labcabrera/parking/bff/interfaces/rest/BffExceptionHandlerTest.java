package org.labcabrera.parking.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

class BffExceptionHandlerTest {

    private final BffExceptionHandler handler = new BffExceptionHandler();

    @Test
    void mapsUnavailableUpstreamToBadGateway() {
        ResponseEntity<ProblemDetail> response = handler.handleResourceAccess(
            new ResourceAccessException("Connection refused", new ConnectException("Connection refused")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties()).containsEntry("code", "GATEWAY_ERROR");
    }

    @Test
    void mapsTimeoutUpstreamToGatewayTimeout() {
        ResponseEntity<ProblemDetail> response = handler.handleResourceAccess(
            new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties()).containsEntry("code", "GATEWAY_TIMEOUT");
    }

    @Test
    void mapsUpstreamServerErrorToBadGateway() {
        RestClientResponseException exception = new RestClientResponseException(
            "Internal Server Error",
            500,
            "Internal Server Error",
            null,
            null,
            null);

        ResponseEntity<ProblemDetail> response = handler.handleRestClientResponse(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties())
            .containsEntry("code", "GATEWAY_ERROR")
            .containsEntry("upstreamStatus", 500);
    }

    @Test
    void keepsUpstreamClientErrorStatus() {
        RestClientResponseException exception = new RestClientResponseException(
            "Not Found",
            404,
            "Not Found",
            null,
            null,
            null);

        ResponseEntity<ProblemDetail> response = handler.handleRestClientResponse(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties())
            .containsEntry("code", "DOWNSTREAM_ERROR")
            .containsEntry("upstreamStatus", 404);
    }
}
