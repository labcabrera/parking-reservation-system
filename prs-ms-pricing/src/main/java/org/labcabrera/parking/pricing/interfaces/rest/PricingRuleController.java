package org.labcabrera.parking.pricing.interfaces.rest;

import java.net.URI;
import java.util.UUID;

import org.labcabrera.parking.pricing.application.service.PricingRuleService;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.interfaces.rest.dto.CreatePricingRuleRequest;
import org.labcabrera.parking.pricing.interfaces.rest.dto.ApiError;
import org.labcabrera.parking.pricing.interfaces.rest.dto.PageResponse;
import org.labcabrera.parking.pricing.interfaces.rest.dto.PricingRuleDto;
import org.labcabrera.parking.pricing.interfaces.rest.dto.UpdatePricingRuleRequest;
import org.labcabrera.parking.pricing.interfaces.rest.mapper.PricingRuleRestMapper;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pricing-rules")
@Tag(name = "Pricing Rules", description = "Create, read, update, delete, and list pricing rule configurations")
@RequiredArgsConstructor
@Validated
public class PricingRuleController {

    private final PricingRuleService service;
    private final PricingRuleRestMapper mapper;

    @PostMapping
    @Operation(
        operationId = "createPricingRule",
        summary = "Create a pricing rule",
        description = "Creates a pricing rule with base, hourly, daily, and tax parameters used by static and dynamic pricing calculations.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Pricing rule created", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = PricingRuleDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid pricing rule request", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<PricingRuleDto> create(@Valid @RequestBody CreatePricingRuleRequest request) {
        PricingRule created = service.create(mapper.toDomain(request));
        return ResponseEntity
            .created(URI.create("/api/v1/pricing-rules/%s".formatted(created.getId())))
            .body(mapper.toDto(created));
    }

    @GetMapping("/{id}")
    @Operation(
        operationId = "getPricingRuleById",
        summary = "Get a pricing rule",
        description = "Returns the pricing rule configuration identified by its id.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Pricing rule found", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = PricingRuleDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Pricing rule not found", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<PricingRuleDto> get(
        @Parameter(description = "Pricing rule identifier", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(service.get(id)));
    }

    @GetMapping
    @Operation(
        operationId = "listPricingRules",
        summary = "List pricing rules",
        description = "Returns a paginated list of pricing rule configurations.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Page of pricing rules", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid pagination request", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<PageResponse<PricingRuleDto>> list(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.list(pageable).map(mapper::toDto)));
    }

    @PutMapping("/{id}")
    @Operation(
        operationId = "updatePricingRule",
        summary = "Update a pricing rule",
        description = "Replaces the pricing rule configuration identified by its id.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Pricing rule updated", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = PricingRuleDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid pricing rule request", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }),
            @ApiResponse(responseCode = "404", description = "Pricing rule not found", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<PricingRuleDto> update(
        @Parameter(description = "Pricing rule identifier", required = true) @PathVariable UUID id,
        @Valid @RequestBody UpdatePricingRuleRequest request) {
        PricingRule updated = service.update(id, mapper.toDomain(id, request));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
        operationId = "deletePricingRule",
        summary = "Delete a pricing rule",
        description = "Deletes the pricing rule configuration identified by its id.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Pricing rule deleted"),
            @ApiResponse(responseCode = "404", description = "Pricing rule not found", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Pricing rule identifier", required = true) @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
