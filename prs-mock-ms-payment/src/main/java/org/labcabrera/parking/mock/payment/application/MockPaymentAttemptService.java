package org.labcabrera.parking.mock.payment.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.mock.payment.infrastructure.ecommerce.ECommerceCallbackClient;
import org.labcabrera.parking.mock.payment.infrastructure.jpa.MockPaymentAttemptRepository;
import org.labcabrera.parking.mock.payment.infrastructure.jpa.entities.MockPaymentAttemptEntity;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.ECommercePaymentCallbackRequest;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentAttemptRequest;
import org.labcabrera.parking.mock.payment.interfaces.rest.dto.PaymentAttemptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockPaymentAttemptService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final MockPaymentAttemptRepository repository;
    private final ECommerceCallbackClient callbackClient;

    @Value("${mock.payment.redirect-base-url:http://localhost:3001/payment-result}")
    private String redirectBaseUrl;

    @Value("${mock.payment.ecommerce-callback-url:http://localhost:8082/api/v1/payment-callbacks/mock}")
    private String defaultEcommerceCallbackUrl;

    @Transactional
    public PaymentAttemptResponse register(PaymentAttemptRequest request) {
        UUID attemptId = request.paymentAttemptId() == null ? UUID.randomUUID() : request.paymentAttemptId();
        return repository.findById(attemptId)
            .map(this::toResponse)
            .orElseGet(() -> createAttempt(attemptId, request));
    }

    @Transactional
    public PaymentAttemptResponse pay(UUID attemptId, String requestedStatus, String customerCallbackUrl) {
        MockPaymentAttemptEntity attempt = repository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment attempt not found: " + attemptId));
        String status = normalizeStatus(requestedStatus);
        attempt.setStatus(status);
        attempt.setPaidAt(LocalDateTime.now());
        if (STATUS_SUCCESS.equals(status)) {
            attempt.setGatewayTransactionId("MOCK-TXN-" + attemptId);
            attempt.setFailureReason(null);
        }
        else {
            attempt.setFailureReason("Payment rejected by mock gateway");
            attempt.setGatewayTransactionId(null);
        }
        if (customerCallbackUrl != null && !customerCallbackUrl.isBlank()) {
            attempt.setCustomerCallbackUrl(customerCallbackUrl);
            attempt.setRedirectUrl(buildRedirectUrl(customerCallbackUrl, attemptId, attempt.getOrderId(), status));
        }
        attempt = repository.save(attempt);
        notifyEcommerce(attempt);
        return toResponse(attempt);
    }

    private PaymentAttemptResponse createAttempt(UUID attemptId, PaymentAttemptRequest request) {
        String customerCallbackUrl = request.callbackUrl() == null || request.callbackUrl().isBlank()
            ? redirectBaseUrl
            : request.callbackUrl();
        String ecommerceCallbackUrl = request.ecommerceCallbackUrl() == null || request.ecommerceCallbackUrl().isBlank()
            ? defaultEcommerceCallbackUrl
            : request.ecommerceCallbackUrl();
        MockPaymentAttemptEntity entity = new MockPaymentAttemptEntity();
        entity.setId(attemptId);
        entity.setOrderId(request.orderId());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setPaymentMethodCode(request.paymentMethodCode());
        entity.setAmount(request.amount());
        entity.setCurrency(request.currency());
        entity.setStatus(STATUS_PENDING);
        entity.setCustomerCallbackUrl(customerCallbackUrl);
        entity.setEcommerceCallbackUrl(ecommerceCallbackUrl);
        entity.setRedirectUrl(buildRedirectUrl(customerCallbackUrl, attemptId, request.orderId(), STATUS_PENDING));
        entity.setCreatedAt(LocalDateTime.now());
        entity = repository.save(entity);
        log.info("Registered mock payment attempt {} for order {}", attemptId, request.orderId());
        return toResponse(entity);
    }

    private void notifyEcommerce(MockPaymentAttemptEntity attempt) {
        if (attempt.getEcommerceCallbackUrl() == null || attempt.getEcommerceCallbackUrl().isBlank()) {
            log.warn("Mock payment attempt {} has no ecommerce callback URL", attempt.getId());
            return;
        }
        callbackClient.notify(attempt.getEcommerceCallbackUrl(), new ECommercePaymentCallbackRequest(
            attempt.getOrderId(),
            attempt.getId(),
            attempt.getStatus(),
            attempt.getGatewayTransactionId(),
            attempt.getFailureReason()));
        log.info("Notified ecommerce callback for payment attempt {}", attempt.getId());
    }

    private PaymentAttemptResponse toResponse(MockPaymentAttemptEntity entity) {
        return new PaymentAttemptResponse(entity.getId(), entity.getStatus(), entity.getRedirectUrl());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || STATUS_SUCCESS.equalsIgnoreCase(status)) {
            return STATUS_SUCCESS;
        }
        if (STATUS_FAILED.equalsIgnoreCase(status)) {
            return STATUS_FAILED;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment status: " + status);
    }

    private String buildRedirectUrl(String baseUrl, UUID attemptId, UUID orderId, String status) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "attemptId=" + attemptId + "&orderId=" + orderId + "&status=" + status;
    }
}
