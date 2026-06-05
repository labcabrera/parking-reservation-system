package org.labcabrera.parking.bff.interfaces.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.bff.generated.client.facilities.api.ReservationsApi;
import org.labcabrera.parking.bff.generated.client.facilities.model.PageResponse;
import org.labcabrera.parking.bff.generated.client.facilities.model.Pageable;
import org.labcabrera.parking.bff.generated.client.facilities.model.Reservation;
import org.labcabrera.parking.bff.generated.client.facilities.model.StartReservationRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationsController {

    private final ReservationsApi reservationsApi;

    public ReservationsController(ReservationsApi reservationsApi) {
        this.reservationsApi = reservationsApi;
    }

    @GetMapping
    public ResponseEntity<PageResponse> listReservations(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) List<String> sort,
        @RequestParam(required = false) UUID facilityId) {
        Pageable pageable = new Pageable().page(page).size(size).sort(sort);
        return reservationsApi.callListWithHttpInfo(start, end, pageable, facilityId);
    }

    @PostMapping
    public ResponseEntity<Reservation> startReservation(
        @RequestBody StartReservationRequest request) {
        return reservationsApi.startWithHttpInfo(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable UUID id) {
        return reservationsApi.getWithHttpInfo(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id) {
        return reservationsApi.cancelWithHttpInfo(id);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmReservation(@PathVariable UUID id) {
        return reservationsApi.confirmWithHttpInfo(id);
    }
}
