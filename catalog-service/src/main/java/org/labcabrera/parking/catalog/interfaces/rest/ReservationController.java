package org.labcabrera.parking.catalog.interfaces.rest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.CancelReservationCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.ConfirmReservationCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.StartReservationCommand;
import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.labcabrera.parking.catalog.infrastructure.jpa.ReservationQueryRepository;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ReservationDto;
import org.labcabrera.parking.catalog.interfaces.rest.dto.ReservationStatusDto;
import org.labcabrera.parking.catalog.interfaces.rest.dto.StartReservationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Create and manage parking reservation holds")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final CommandGateway commandGateway;
    private final ReservationQueryRepository reservationRepository;

    @Value("${catalog.reservation.hold-minutes:10}")
    private int holdMinutes;

    @PostMapping
    @Operation(summary = "Start a reservation hold (10 min TTL)")
    public ResponseEntity<ReservationDto> start(@Valid @RequestBody StartReservationRequest request) {
        log.info("Received StartReservationRequest for facility {} from {} to {}",
            request.facilityId(), request.checkIn(), request.checkOut());


        UUID reservationId = UUID.randomUUID();
        // TODO integrate with security to extract real user id
        String userId = "user-test";
        var command = new StartReservationCommand(
            reservationId, request.facilityId(), userId, request.checkIn(), request.checkOut());
        commandGateway.sendAndWait(command, 5, TimeUnit.SECONDS);

        Reservation saved = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalStateException("Reservation not persisted: " + reservationId));
        ReservationDto body = new ReservationDto(reservationId, saved.getExpiresAt());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(reservationId)
            .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation status (status, estimated price, expiry)")
    public ResponseEntity<ReservationStatusDto> get(@PathVariable UUID id) {
        return reservationRepository.findById(id)
            .map(this::toDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a HELD reservation")
    public ResponseEntity<Void> confirm(@PathVariable UUID id) {
        log.info("Received confirm request for reservation {}", id);
        commandGateway.sendAndWait(new ConfirmReservationCommand(id), 5, TimeUnit.SECONDS);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation and release inventory")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        log.info("Received cancel request for reservation {}", id);
        commandGateway.sendAndWait(new CancelReservationCommand(id, "user-cancelled"), 5, TimeUnit.SECONDS);
        return ResponseEntity.noContent().build();
    }

    private ReservationStatusDto toDto(Reservation r) {
        return new ReservationStatusDto(
            r.getId(),
            r.getFacilityId(),
            r.getUserId(),
            r.getCheckIn(),
            r.getCheckOut(),
            r.getStatus().name(),
            r.getEstimatedPrice(),
            r.getCurrency(),
            r.getExpiresAt(),
            r.getFailureReason());
    }
}
