package org.labcabrera.parking.catalog.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.labcabrera.parking.catalog.application.dto.FacilityResult;
import org.labcabrera.parking.catalog.application.queries.SearchParkingQuery;
import org.labcabrera.parking.catalog.application.port.inbound.SearchParkingPort;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalog", description = "Parking facility search and availability")
public class CatalogController {

    private final SearchParkingPort searchParkingPort;

    public CatalogController(SearchParkingPort searchParkingPort) {
        this.searchParkingPort = searchParkingPort;
    }

    @GetMapping("/search")
    @Operation(summary = "Search parking facilities",
            description = "Full-text search across parking facilities filtered by availability window")
    @ApiResponse(responseCode = "200", description = "Paginated list of matching facilities")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    public ResponseEntity<Page<FacilityResult>> search(
            @Parameter(description = "Free-text search (name or city)", required = true)
            @RequestParam String q,
            @Parameter(description = "Check-in datetime (ISO 8601)", required = true, example = "2026-06-03T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime checkIn,
            @Parameter(description = "Check-out datetime (ISO 8601)", required = true, example = "2026-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime checkOut,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        SearchParkingQuery query = SearchParkingQuery.of(q, checkIn, checkOut, page, size);
        Page<FacilityResult> results = searchParkingPort.search(query);
        return ResponseEntity.ok(results);
    }
}
