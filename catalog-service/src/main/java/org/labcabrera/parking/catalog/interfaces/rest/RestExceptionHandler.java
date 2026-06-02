package org.labcabrera.parking.catalog.interfaces.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.labcabrera.parking.catalog.domain.exception.DomainException;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
        log.error("Caugth Domain exception: code={}, message={}", ex.getMessage(), ex);
        var apiError = fromDomainException(ex);
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(apiError);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurityException(SecurityException ex) {
        log.error("Caugth security exception: code={}, message={}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiError("FORBIDDEN", ex.getMessage(), LocalDateTime.now(), new ArrayList<>()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation exception", ex);
        var details = new ArrayList<String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.add("%s, %s".formatted(fieldName, errorMessage));
        });
        var apiError = new ApiError("msg.err.validation-error", "msg.err.validation-error",
            LocalDateTime.now(), details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        // In this cases avoid stack trace pollution
        log.warn("Illegal argument exception. {}", ex.getMessage());
        ApiError error = new ApiError("ILLEGAL_ARGUMENT", ex.getMessage(), LocalDateTime.now(),
            new ArrayList<>());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatchException(
        MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch exception", ex);
        Class<?> requiredType = ex.getRequiredType();
        String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";
        String message = String.format("Parameter '%s' should be of type %s", ex.getName(), typeName);
        ApiError error = new ApiError("msg.err.method-argument-type-mismatch", message,
            LocalDateTime.now(), new ArrayList<>());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("HTTP message not readable exception", ex);
        ApiError error = new ApiError("msg.err.http-message-not-readable",
            "msg.err.http-message-not-readable", LocalDateTime.now(), new ArrayList<>());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.error("No resource found exception", ex);
        ApiError error = new ApiError("msg.err.no-resource-found", "msg.err.no-resource-found",
            LocalDateTime.now(), new ArrayList<>());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("No handler found exception", ex);
        ApiError error = new ApiError("msg.err.no-handler-found", "msg.err.no-handler-found",
            LocalDateTime.now(), new ArrayList<>());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Unexpected exception", ex);
        ApiError error = new ApiError("INTERNAL_SERVER_ERROR", "An unexpected error occurred",
            LocalDateTime.now(), new ArrayList<>());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ApiError fromDomainException(DomainException ex) {
        var list = new ArrayList<String>();
        list.add("Exception status: %s".formatted(ex.getStatus()));
        list.add("Exception message: %s".formatted(ex.getMessage()));
        list.add("Exception class: %s".formatted(ex.getClass().getName()));
       return new ApiError(ex.getMessage(), ex.getMessage(),LocalDateTime.now(), list);
    }

}
