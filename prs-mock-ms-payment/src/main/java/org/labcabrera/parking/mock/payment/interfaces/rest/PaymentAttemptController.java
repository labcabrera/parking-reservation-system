package org.labcabrera.parking.mock.payment.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.labcabrera.parking.mock.payment.application.MockPaymentAttemptService;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentAttemptRequest;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentAttemptResponse;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentCaptureRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-attempts")
@Tag(name = "Payment Attempts", description = "Mock payment platform API")
public class PaymentAttemptController {

    private final MockPaymentAttemptService service;

    public PaymentAttemptController(MockPaymentAttemptService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a payment attempt", description = "Stores a payment attempt and returns the redirect URL for the mock gateway UI")
    public ResponseEntity<PaymentAttemptResponse> attempt(@Valid @RequestBody PaymentAttemptRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/{attemptId}/pay")
    @Operation(summary = "Capture a mock payment", description = "Marks a stored payment attempt as paid or failed and notifies ecommerce")
    public ResponseEntity<PaymentAttemptResponse> pay(
        @PathVariable UUID attemptId,
        @RequestBody(required = false) PaymentCaptureRequest request) {
        String status = request == null ? null : request.status();
        String callbackUrl = request == null ? null : request.callbackUrl();
        return ResponseEntity.ok(service.pay(attemptId, status, callbackUrl));
    }
}
