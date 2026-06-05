package org.labcabrera.parking.bff.interfaces.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.labcabrera.sample.front.generated.client.geo.api.ParkingFacilitiesApi;
import org.labcabrera.sample.front.generated.client.geo.model.CreateParkingFacilityRequest;
import org.labcabrera.sample.front.generated.client.geo.model.FacilityAvailability;
import org.labcabrera.sample.front.generated.client.geo.model.InventorySlot;
import org.labcabrera.sample.front.generated.client.geo.model.ParkingFacility;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parking-facilities")
public class FacilitiesController {

    private final ParkingFacilitiesApi parkingFacilitiesApi;

    public FacilitiesController(ParkingFacilitiesApi parkingFacilitiesApi) {
        this.parkingFacilitiesApi = parkingFacilitiesApi;
    }

    @GetMapping("/availability")
    public ResponseEntity<List<FacilityAvailability>> searchAvailability(
            @RequestParam String q,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut,
            @RequestParam(required = false) Integer limit) {
        return parkingFacilitiesApi.searchAvailabilityWithHttpInfo(q, checkIn, checkOut, limit);
    }

    @GetMapping("/{parkingFacilityId}")
    public ResponseEntity<ParkingFacility> getParkingFacilityById(
            @PathVariable String parkingFacilityId) {
        return parkingFacilitiesApi.getParkingFacilityByIdWithHttpInfo(parkingFacilityId);
    }

    @GetMapping("/{parkingFacilityId}/inventory")
    public ResponseEntity<List<InventorySlot>> getFacilityInventory(
            @PathVariable String parkingFacilityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return parkingFacilitiesApi.getFacilityInventoryWithHttpInfo(parkingFacilityId, start, end);
    }

    @PostMapping
    public ResponseEntity<ParkingFacility> createParkingFacility(
            @RequestBody CreateParkingFacilityRequest request) {
        return parkingFacilitiesApi.createParkingFacilityWithHttpInfo(request);
    }
}
