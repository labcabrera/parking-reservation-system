package org.labcabrera.parking.bff.interfaces.rest;

import org.labcabrera.parking.bff.generated.client.ecommerce.api.PaymentMethodsApi;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.PageResponsePaymentMethod;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-methods")
@Tag(name = "Payment methods", description = "List available payment methods for checkout")
public class PaymentMethodsController {

    private final PaymentMethodsApi paymentMethodsApi;

    public PaymentMethodsController(PaymentMethodsApi paymentMethodsApi) {
        this.paymentMethodsApi = paymentMethodsApi;
    }

    @GetMapping
    public ResponseEntity<PageResponsePaymentMethod> list(
        @RequestParam(defaultValue = "true") boolean activeOnly,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) List<String> sort) {
        Pageable pageable = new Pageable().page(page).size(size).sort(sort);
        return paymentMethodsApi.callListWithHttpInfo(pageable, activeOnly);
    }
}
