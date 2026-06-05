package org.labcabrera.parking.pricing.interfaces.rest;

import java.net.URI;
import java.util.UUID;

import org.labcabrera.parking.pricing.application.service.PricingRuleService;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.interfaces.rest.dto.CreatePricingRuleRequest;
import org.labcabrera.parking.pricing.interfaces.rest.dto.PricingRuleDto;
import org.labcabrera.parking.pricing.interfaces.rest.dto.UpdatePricingRuleRequest;
import org.labcabrera.parking.pricing.interfaces.rest.mapper.PricingRuleRestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pricing-rules")
@RequiredArgsConstructor
@Validated
public class PricingRuleController {

    private final PricingRuleService service;
    private final PricingRuleRestMapper mapper;

    @PostMapping
    public ResponseEntity<PricingRuleDto> create(@Valid @RequestBody CreatePricingRuleRequest request) {
        PricingRule created = service.create(mapper.toDomain(request));
        return ResponseEntity
            .created(URI.create("/api/v1/pricing-rules/%s".formatted(created.getId())))
            .body(mapper.toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingRuleDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(service.get(id)));
    }

    @GetMapping
    public ResponseEntity<Page<PricingRuleDto>> list(@RequestParam(required = false) UUID facilityId, Pageable pageable) {
        return ResponseEntity.ok(service.list(facilityId, pageable).map(mapper::toDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingRuleDto> update(@PathVariable UUID id, @Valid @RequestBody UpdatePricingRuleRequest request) {
        PricingRule existing = service.get(id);
        PricingRule updated = service.update(id, mapper.toDomain(id, existing.getFacilityId(), request));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
