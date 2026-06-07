package org.labcabrera.parking.pricing.interfaces.rest;

import org.labcabrera.parking.pricing.application.cqrs.handler.DynamicPricingQueryHandler;
import org.labcabrera.parking.pricing.domain.valueobject.DynamicPricingResult;
import org.labcabrera.parking.pricing.interfaces.rest.dto.ApiError;
import org.labcabrera.parking.pricing.interfaces.rest.dto.DynamicRateRequest;
import org.labcabrera.parking.pricing.interfaces.rest.dto.DynamicRateResponse;
import org.labcabrera.parking.pricing.interfaces.rest.mapper.DynamicPricingRestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "Pricing", description = "Pricing calculations")
@RequiredArgsConstructor
@Validated
public class DynamicPricingController {

    private final DynamicPricingQueryHandler queryHandler;
    private final DynamicPricingRestMapper mapper;

    @PostMapping("/dynamic-rate")
    @Operation(
        operationId = "calculateDynamicRate",
        summary = "Calculate a dynamic price from slot occupancy",
        description = "Calculates a dynamic price using a pricing rule and the occupancy rate of the involved slots. "
            + "SHORT slots use half-hour pricing derived from the rule hourly rate; LONG slots use the rule daily rate.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Dynamic price calculated", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = DynamicRateResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid dynamic pricing request", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }),
            @ApiResponse(responseCode = "404", description = "Pricing rule not found", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<DynamicRateResponse> calculateDynamicRate(@Valid @RequestBody DynamicRateRequest request) {
        DynamicPricingResult result = queryHandler.handle(mapper.toQuery(request));
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
