package org.labcabrera.parking.catalog.application.cqrs.handler;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.catalog.application.cqrs.query.GetParkingFacilitiesQuery;
import org.labcabrera.parking.catalog.application.cqrs.query.GetParkingFacilityByIdQuery;
import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.catalog.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
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
        return repository.findById(query.facilityId())
            .orElseThrow(() -> new EntityNotFoundException("Parking facility not found with id: " + query.facilityId()));
    }

    //NOTE: Axon cant handle properly generic types, so we need to cast the response type in the controller
    @SuppressWarnings("rawtypes")
    @QueryHandler
    public Page handle(GetParkingFacilitiesQuery query) {
        log.info("Handling GetParkingFacilitiesQuery {}", query);
        return repository.findByRsql(query.rsql(), query.pageable());
    }

}
