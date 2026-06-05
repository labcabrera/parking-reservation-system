package org.labcabrera.parking.ecommerce.interfaces.rest;

import org.labcabrera.parking.ecommerce.application.service.PaymentMethodService;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.PaymentMethodsResponse;
import org.labcabrera.parking.ecommerce.interfaces.rest.mapper.PaymentMethodMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Gateway", description = "Checkout payment gateway reference data")
@RequiredArgsConstructor
public class PaymentGatewayController {

    private final PaymentMethodService service;
    private final PaymentMethodMapper mapper;

    @GetMapping("/methods")
    @Operation(summary = "List payment methods available at checkout")
    public ResponseEntity<PaymentMethodsResponse> getAvailablePaymentMethods(
        @PageableDefault(size = 50, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        var methods = service.list(pageable, true)
            .map(mapper::toDto)
            .toList();
        return ResponseEntity.ok(new PaymentMethodsResponse(methods));
    }
}
