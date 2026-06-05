package org.labcabrera.parking.facilities.application.cqrs.handler;

import java.util.List;
import java.util.Optional;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.facilities.application.cqrs.query.GetFacilityInventoryQuery;
import org.labcabrera.parking.facilities.application.cqrs.query.GetParkingFacilitiesQuery;
import org.labcabrera.parking.facilities.application.cqrs.query.GetParkingFacilityByIdQuery;
import org.labcabrera.parking.facilities.application.service.FacilityAvailabilitySearchService;
import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.facilities.domain.port.ParkingFacilityRepository;
import org.labcabrera.parking.facilities.domain.valueobject.InventorySlot;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ParkingFacilityQueryHandler {

    private final ParkingFacilityRepository repository;
    private final FacilityAvailabilitySearchService availabilitySearchService;

    @QueryHandler
    public Optional<ParkingFacility> handle(GetParkingFacilityByIdQuery query) {
        log.info("Handling GetParkingFacilityByIdQuery {}", query);
        return repository.findById(query.facilityId());
    }

    //NOTE: Axon cant handle properly generic types, so we need to cast the response type in the controller
    @SuppressWarnings("rawtypes")
    @QueryHandler
    public Page handle(GetParkingFacilitiesQuery query) {
        log.info("Handling GetParkingFacilitiesQuery {}", query);
        return repository.findByRsql(query.rsql(), query.pageable());
    }

    @QueryHandler
    public List<InventorySlot> handle(GetFacilityInventoryQuery q) {
        return availabilitySearchService.getInventorySlots(q.facilityId(), q.start(), q.end());
    }

}
