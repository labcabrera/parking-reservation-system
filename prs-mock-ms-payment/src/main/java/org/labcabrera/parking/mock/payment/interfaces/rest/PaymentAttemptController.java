package org.labcabrera.parking.mock.payment.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentAttemptRequest;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentAttemptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-attempts")
@Tag(name = "Payment Attempts", description = "Mock payment platform API")
public class PaymentAttemptController {

    @Value("${mock.payment.redirect-base-url:http://localhost:3001/payment-result}")
    private String redirectBaseUrl;

    @PostMapping
    @Operation(summary = "Submit a payment attempt", description = "Simulates a payment and returns the redirect URL on success")
    public ResponseEntity<PaymentAttemptResponse> attempt(@Valid @RequestBody PaymentAttemptRequest request) {
        UUID attemptId = UUID.randomUUID();
        String baseUrl = request.callbackUrl() == null || request.callbackUrl().isBlank()
            ? redirectBaseUrl
            : request.callbackUrl();
        String separator = baseUrl.contains("?") ? "&" : "?";
        String redirectUrl = baseUrl + separator + "attemptId=" + attemptId + "&orderId=" + request.orderId() + "&status=SUCCESS";
        PaymentAttemptResponse response = new PaymentAttemptResponse(attemptId, "SUCCESS", redirectUrl);
        return ResponseEntity.ok(response);
    }
}
