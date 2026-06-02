package org.labcabrera.parking.catalog.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.CreateFacilitySearchCommand;
import org.labcabrera.parking.catalog.application.cqrs.query.SearchParkingQuery;
import org.labcabrera.parking.catalog.application.dto.SearchResponse;
import org.labcabrera.parking.catalog.interfaces.rest.dto.CatalogSearchRequest;
import org.labcabrera.parking.catalog.application.port.inbound.SearchParkingPort;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalog", description = "Parking facility search and availability")
@AllArgsConstructor
public class CatalogController {

    private final CommandGateway commandGateway;
    private final SearchParkingPort searchParkingPort;
    private final AvailabilityStreamRegistry availabilityStreamRegistry;

    @GetMapping("/search")
    @Operation(summary = "Search parking facilities", description = "Full-text search across parking facilities filtered by availability window")
    @ApiResponse(responseCode = "200", description = "Search results with session ID and estimated prices")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    public ResponseEntity<SearchResponse> search(
        @Parameter(description = "Free-text search (name or city)", required = true) @RequestParam String q,
        @Parameter(description = "Check-in datetime (ISO 8601)", required = true, example = "2026-06-03T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
        @Parameter(description = "Check-out datetime (ISO 8601)", required = true, example = "2026-06-05T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut,
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Center latitude for proximity search") @RequestParam(required = false) Double lat,
        @Parameter(description = "Center longitude for proximity search") @RequestParam(required = false) Double lng,
        @Parameter(description = "Radius in km for proximity search") @RequestParam(required = false) Double radiusKm,
        @Parameter(description = "Required facility features (AND filter)") @RequestParam(required = false) List<String> features) {
        SearchParkingQuery query = SearchParkingQuery.of(q, checkIn, checkOut, page, size, lat, lng, radiusKm, features);
        SearchResponse response = searchParkingPort.search(query);
        MDC.put("searchSessionId", response.getSearchSessionId());
        try {
            return ResponseEntity.ok(response);
        }
        finally {
            MDC.remove("searchSessionId");
        }
    }

    @GetMapping(value = "/availability/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE stream for availability changes", description = "Server-Sent Events stream for real-time availability updates for a facility")
    @ApiResponse(responseCode = "200", description = "SSE stream established")
    @ApiResponse(responseCode = "400", description = "Missing facilityId parameter")
    public SseEmitter streamAvailability(
        @Parameter(description = "Facility UUID to subscribe to", required = true) @RequestParam String facilityId) {

        if (facilityId == null || facilityId.isBlank()) {
            throw new IllegalArgumentException("facilityId is required");
        }
        // Validate UUID format
        UUID.fromString(facilityId);
        return availabilityStreamRegistry.register(facilityId);
    }

    @PostMapping("/search")
    @Operation(summary = "Initiate parking search with command", description = "Initiates a parking search using a command, returning a search session ID for tracking")
    @ApiResponse(responseCode = "200", description = "Search command accepted with session ID")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    public ResponseEntity<String> initiateSearch(CatalogSearchRequest request) {
        String searchSessionId = UUID.randomUUID().toString();
        var command = new CreateFacilitySearchCommand(
            request.query(),
            request.checkIn(),
            request.checkOut(),
            request.features());
        var response = commandGateway.sendAndWait(command, 5, TimeUnit.SECONDS);
        return ResponseEntity.ok(searchSessionId);
    }
}
