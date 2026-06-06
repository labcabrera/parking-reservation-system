package org.labcabrera.parking.ecommerce.interfaces.rest;

import java.net.URI;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.service.PaymentMethodService;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.CreatePaymentMethodRequest;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.PageResponse;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.Pagination;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.PaymentMethodDto;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.UpdatePaymentMethodRequest;
import org.labcabrera.parking.ecommerce.interfaces.rest.mapper.PaymentMethodMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payment-methods")
@Tag(name = "Payment Methods", description = "Payment methods supported by the ecommerce gateway")
@RequiredArgsConstructor
@Validated
public class PaymentMethodController {

    private final PaymentMethodService service;
    private final PaymentMethodMapper mapper;

    @PostMapping
    @Operation(summary = "Register a supported payment method")
    public ResponseEntity<PaymentMethodDto> create(@Valid @RequestBody CreatePaymentMethodRequest request) {
        PaymentMethodDto dto = mapper.toDto(service.create(mapper.toDomain(request)));
        return ResponseEntity
            .created(URI.create("/api/v1/payment-methods/%s".formatted(dto.id())))
            .body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a supported payment method by id")
    public ResponseEntity<PaymentMethodDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(service.get(id)));
    }

    @GetMapping
    @Operation(summary = "List supported payment methods")
    public ResponseEntity<PageResponse<PaymentMethodDto>> list(
        @RequestParam(defaultValue = "true") boolean activeOnly,
        Pageable pageable) {
        Page<PaymentMethodDto> page = service.list(defaultSort(pageable), activeOnly).map(mapper::toDto);
        var response = new PageResponse<>(
            page.getContent(),
            new Pagination(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supported payment method")
    public ResponseEntity<PaymentMethodDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePaymentMethodRequest request) {
        var existing = service.get(id);
        var source = mapper.toDomain(existing, request);
        return ResponseEntity.ok(mapper.toDto(service.update(id, source)));
    }

    private Pageable defaultSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by("displayOrder").ascending().and(Sort.by("displayName").ascending()));
    }
}
