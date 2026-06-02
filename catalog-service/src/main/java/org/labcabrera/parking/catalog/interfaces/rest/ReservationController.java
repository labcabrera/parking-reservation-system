package org.labcabrera.parking.catalog.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.StartReservationCommand;
import org.labcabrera.parking.catalog.interfaces.rest.dto.CatalogSearchRequest;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ReservationDto;
import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/reservation")
@Tag(name = "Catalog", description = "Parking facility search and availability")
@AllArgsConstructor
public class ReservationController {

    private final CommandGateway commandGateway;

    @PostMapping()
    @Operation(summary = "Initiate parking search with command", description = "Initiates a parking search using a command, returning a search session ID for tracking")
    @ApiResponse(responseCode = "201", description = "Search command accepted with session ID")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    public ResponseEntity<ReservationDto> initiateSearch(CatalogSearchRequest request) {
        var command = new StartReservationCommand(
            null,
            request.query(),
            request.checkIn(),
            request.checkOut());
        Reservation response = commandGateway.sendAndWait(command, 5, TimeUnit.SECONDS);
        String id = response.getId().toString();
        ReservationDto dto = new ReservationDto(id);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
        return ResponseEntity.created(location).body(dto);
    }
}
