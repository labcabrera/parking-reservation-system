package org.labcabrera.parking.pricing.interfaces.rest;

import java.time.Instant;
import java.util.List;

import org.labcabrera.parking.pricing.domain.exception.DomainException;
import org.labcabrera.parking.pricing.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.pricing.interfaces.rest.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleDomain(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .toList();
        return error(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        return ResponseEntity.status(status)
            .body(new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details));
    }

    private String formatFieldError(FieldError error) {
        return "%s %s".formatted(error.getField(), error.getDefaultMessage());
    }
}
