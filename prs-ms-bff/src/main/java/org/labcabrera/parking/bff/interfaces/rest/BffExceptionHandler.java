package org.labcabrera.parking.bff.interfaces.rest;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class BffExceptionHandler {

    private static final String GATEWAY_ERROR_CODE = "GATEWAY_ERROR";
    private static final String GATEWAY_TIMEOUT_CODE = "GATEWAY_TIMEOUT";
    private static final String DOWNSTREAM_ERROR_CODE = "DOWNSTREAM_ERROR";

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ProblemDetail> handleResourceAccess(ResourceAccessException exception) {
        if (hasCause(exception, SocketTimeoutException.class) || hasCause(exception, HttpTimeoutException.class)) {
            return problem(
                HttpStatus.GATEWAY_TIMEOUT,
                GATEWAY_TIMEOUT_CODE,
                "Gateway timeout",
                "The upstream API did not respond in time.");
        }

        return problem(
            HttpStatus.BAD_GATEWAY,
            GATEWAY_ERROR_CODE,
            "Bad gateway",
            "The upstream API is not accessible.");
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ProblemDetail> handleRestClientResponse(RestClientResponseException exception) {
        HttpStatusCode upstreamStatus = exception.getStatusCode();
        HttpStatusCode responseStatus = upstreamStatus.is5xxServerError()
            ? HttpStatus.BAD_GATEWAY
            : upstreamStatus;

        ProblemDetail body = ProblemDetail.forStatus(responseStatus);
        body.setTitle(upstreamStatus.is5xxServerError() ? "Bad gateway" : "Downstream API error");
        body.setDetail(upstreamStatus.is5xxServerError()
            ? "The upstream API returned an error response."
            : "The upstream API rejected the request.");
        body.setProperty("code", upstreamStatus.is5xxServerError() ? GATEWAY_ERROR_CODE : DOWNSTREAM_ERROR_CODE);
        body.setProperty("upstreamStatus", upstreamStatus.value());

        return ResponseEntity.status(responseStatus).body(body);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ProblemDetail> handleRestClient(RestClientException exception) {
        if (hasCause(exception, SocketTimeoutException.class) || hasCause(exception, HttpTimeoutException.class)) {
            return problem(
                HttpStatus.GATEWAY_TIMEOUT,
                GATEWAY_TIMEOUT_CODE,
                "Gateway timeout",
                "The upstream API did not respond in time.");
        }

        if (hasCause(exception, ConnectException.class)
            || hasCause(exception, NoRouteToHostException.class)
            || hasCause(exception, UnknownHostException.class)) {
            return problem(
                HttpStatus.BAD_GATEWAY,
                GATEWAY_ERROR_CODE,
                "Bad gateway",
                "The upstream API is not accessible.");
        }

        return problem(
            HttpStatus.BAD_GATEWAY,
            GATEWAY_ERROR_CODE,
            "Bad gateway",
            "The upstream API call failed.");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String title, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        body.setProperty("code", code);
        return ResponseEntity.status(status).body(body);
    }

    private boolean hasCause(Throwable exception, Class<? extends Throwable> causeType) {
        Throwable current = exception;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
