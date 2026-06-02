package org.labcabrera.parking.catalog.application.cqrs.handler;

import java.util.UUID;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.catalog.application.cqrs.query.GetParkingFacilitiesQuery;
import org.labcabrera.parking.catalog.application.cqrs.query.GetParkingFacilityByIdQuery;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ParkingFacilityQueryHandler {

    private final ParkingFacilityRepository repository;

    @QueryHandler
    public ParkingFacility handle(GetParkingFacilityByIdQuery query) {
        log.info("Handling GetParkingFacilityByIdQuery {}", query);
        FacilityId facilityId = FacilityId.of(UUID.fromString(query.parkingFacilityId()));
        return repository.findById(facilityId)
            .orElseThrow(() -> new RuntimeException("Parking facility not found with id: " + query.parkingFacilityId()));
    }

    @QueryHandler
    public Page<ParkingFacility> handle(GetParkingFacilitiesQuery query) {
        log.info("Handling GetParkingFacilitiesQuery {}", query);
        return repository.findByRsql(query.rsql(), query.pageable());
    }

}
