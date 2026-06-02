package org.labcabrera.parking.catalog.interfaces.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.CreateParkingFacilityCommand;
import org.labcabrera.parking.catalog.application.cqrs.query.GetParkingFacilitiesQuery;
import org.labcabrera.parking.catalog.application.cqrs.query.GetParkingFacilityByIdQuery;
import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityId;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ApiError;
import org.labcabrera.parking.catalog.interfaces.rest.dto.CreateParkingFacilityRequest;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ParkingFacilityDto;
import org.labcabrera.parking.catalog.interfaces.rest.mapper.ParkingFacilityMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/parking-facilities")
@Tag(name = "Parking Facilities", description = "Parking facility CRUD operations")
@AllArgsConstructor
@Slf4j
public class FacilityParkingController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final ParkingFacilityMapper mapper;

    @GetMapping("/{parkingFacilityId}")
    @Operation(operationId = "getParkingFacilityById", summary = "Get parking facility by id", description = "Get country by id", responses = {
        @ApiResponse(responseCode = "200", description = "Parking facility", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingFacilityDto.class)) }),
        @ApiResponse(responseCode = "404", description = "Not found", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) }
    // security = {
    //     @SecurityRequirement(name = "oidc"),
    //     @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<ParkingFacilityDto> getById(@PathVariable String parkingFacilityId) {
        var id = FacilityId.of(UUID.fromString(parkingFacilityId));
        var query = new GetParkingFacilityByIdQuery(id);
        ParkingFacility parkingFacility = queryGateway.query(query, ParkingFacility.class).join();
        return ResponseEntity.ok(mapper.toDto(parkingFacility));
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    @Operation(operationId = "searchParkingFacilities", summary = "Search parking facilities", description = "Search parking facilities using RSQL and pagination", responses = {
        @ApiResponse(responseCode = "200", description = "Paged list of parking facilities", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParkingFacilityDto.class))) }),
        @ApiResponse(responseCode = "400", description = "Invalid query", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<Page<ParkingFacilityDto>> search(@RequestParam(required = false) String rsql,
        @ParameterObject Pageable pageable) {
        var query = new GetParkingFacilitiesQuery(rsql, pageable);
        Page<ParkingFacility> page = queryGateway.query(query, Page.class).join();
        Page<ParkingFacilityDto> dtoPage = page.map(mapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    @Operation(operationId = "createParkingFacility", summary = "Create a new parking facility", description = "Create a new parking facility with the provided details", responses = {
        @ApiResponse(responseCode = "200", description = "Created parking facility", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingFacilityDto.class)) }),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)) }) })
    public ResponseEntity<ParkingFacilityDto> create(@RequestBody CreateParkingFacilityRequest request) {
        log.debug("Creating parking facility with name: {}", request.name());
        var command = new CreateParkingFacilityCommand(
            request.name(),
            request.city(),
            request.address(),
            request.location(),
            request.totalSpots(),
            request.tags(),
            request.status(),
            request.cancellationPolicy(),
            request.pricingRule());
        ParkingFacility parkingFacility = commandGateway.sendAndWait(command);
        return ResponseEntity.ok(mapper.toDto(parkingFacility));
    }

}
