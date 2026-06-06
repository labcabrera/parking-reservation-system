package org.labcabrera.parking.facilities.interfaces.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.labcabrera.parking.facilities.application.cqrs.command.CancelReservationCommand;
import org.labcabrera.parking.facilities.application.cqrs.command.ConfirmReservationCommand;
import org.labcabrera.parking.facilities.application.cqrs.command.StartReservationCommand;
import org.labcabrera.parking.facilities.application.cqrs.query.FindReservationsQuery;
import org.labcabrera.parking.facilities.application.cqrs.query.GetReservationByIdQuery;
import org.labcabrera.parking.facilities.domain.aggregate.Reservation;
import org.labcabrera.parking.facilities.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.facilities.interfaces.rest.dto.PageResponse;
import org.labcabrera.parking.facilities.interfaces.rest.dto.Pagination;
import org.labcabrera.parking.facilities.interfaces.rest.dto.ReservationDto;
import org.labcabrera.parking.facilities.interfaces.rest.dto.StartReservationRequest;
import org.labcabrera.parking.facilities.interfaces.rest.mapper.ReservationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final QueryGateway queryGateway;
    private final ReservationMapper reservationMapper;

    @PostMapping
    @Operation(summary = "Start a reservation hold")
    public ResponseEntity<ReservationDto> start(@Valid @RequestBody StartReservationRequest request) throws URISyntaxException {
        log.info("Received StartReservationRequest for facility {} from {} to {}",
            request.facilityId(), request.checkIn(), request.checkOut());

        // TODO integrate with security to extract real user id
        UUID reservationId = UUID.randomUUID();
        String userId = "user-test";
        var command = new StartReservationCommand(reservationId, request.facilityId(), userId, request.checkIn(), request.checkOut());
        commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);

        //TODO leer del comand directamente
        var query = new GetReservationByIdQuery(reservationId);
        ResponseType<Optional<Reservation>> responseType = ResponseTypes.optionalInstanceOf(Reservation.class);
        Reservation readed = queryGateway.query(query, responseType).join()
            .orElseThrow(() -> new EntityNotFoundException("Reservation %s not found".formatted(reservationId)));
        URI uri = new URI("/api/v1/reservations/%s".formatted(reservationId));
        return ResponseEntity.created(uri).body(reservationMapper.toDto(readed));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation status (status, estimated price, expiry)")
    public ResponseEntity<ReservationDto> get(@PathVariable UUID id) {

        var query = new GetReservationByIdQuery(id);
        ResponseType<Optional<Reservation>> responseType = ResponseTypes.optionalInstanceOf(Reservation.class);
        Reservation reservation = queryGateway.query(query, responseType).join()
            .orElseThrow(() -> new EntityNotFoundException("Reservation %s not found".formatted(id)));
        return ResponseEntity.ok(reservationMapper.toDto(reservation));
    }

    @GetMapping
    @Operation(summary = "List reservations overlapping a time window")
    @SuppressWarnings("unchecked")
    public ResponseEntity<PageResponse<ReservationDto>> list(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @RequestParam(required = false) UUID facilityId,
        Pageable pageable) {

        log.debug("Received list reservations request for facility {} from {} to {}", facilityId, start, end);
        var query = new FindReservationsQuery(start, end, facilityId, pageable);
        var respType = ResponseTypes.instanceOf(Page.class);
        Page<Reservation> page = queryGateway.query(query, respType).join();
        Page<ReservationDto> dtoPage = page.map(reservationMapper::toDto);
        var response = new PageResponse<>(
            dtoPage.getContent(),
            new Pagination(dtoPage.getNumber(), dtoPage.getSize(), dtoPage.getTotalElements(), dtoPage.getTotalPages()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a HELD reservation")
    public ResponseEntity<ReservationDto> confirm(@PathVariable UUID id) {
        log.info("Received confirm request for reservation {}", id);
        commandGateway.sendAndWait(new ConfirmReservationCommand(id), 5, TimeUnit.SECONDS);
        var query = new GetReservationByIdQuery(id);
        ResponseType<Optional<Reservation>> responseType = ResponseTypes.optionalInstanceOf(Reservation.class);
        Reservation reservation = queryGateway.query(query, responseType).join()
            .orElseThrow(() -> new EntityNotFoundException("Reservation %s not found".formatted(id)));
        return ResponseEntity.ok(reservationMapper.toDto(reservation));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation and release inventory")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        log.info("Received cancel request for reservation {}", id);
        commandGateway.sendAndWait(new CancelReservationCommand(id, "user-cancelled"), 5, TimeUnit.SECONDS);
        return ResponseEntity.noContent().build();
    }

}
