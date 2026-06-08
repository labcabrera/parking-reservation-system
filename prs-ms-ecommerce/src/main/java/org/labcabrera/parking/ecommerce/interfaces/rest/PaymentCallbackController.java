package org.labcabrera.parking.ecommerce.interfaces.rest;

import org.labcabrera.parking.ecommerce.application.service.OrderPaymentService;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.PaymentCallbackRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/payment-callbacks")
@Tag(name = "Payment callbacks", description = "Payment provider callback endpoints")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentCallbackController {

    private final OrderPaymentService orderPaymentService;

    @PostMapping("/mock")
    @Operation(summary = "Receive mock payment result")
    public ResponseEntity<Void> receiveMockPayment(@Valid @RequestBody PaymentCallbackRequest request) {
        log.info("Received mock payment callback for order {} attempt {} status {}",
            request.orderId(), request.paymentAttemptId(), request.status());
        orderPaymentService.completePayment(
            request.orderId(),
            request.paymentAttemptId(),
            request.status(),
            request.gatewayTransactionId(),
            request.failureReason());
        return ResponseEntity.accepted().build();
    }
}
