package org.labcabrera.parking.catalog.application.cqrs.handler;

import java.util.List;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

import org.labcabrera.parking.catalog.application.cqrs.query.GetFacilityInventoryQuery;
import org.labcabrera.parking.catalog.application.service.FacilityAvailabilitySearchService;
import org.labcabrera.parking.catalog.domain.valueobject.InventorySlot;

@Component
@AllArgsConstructor
public class FacilityInventoryQueryHandler {

    private final FacilityAvailabilitySearchService availabilitySearchService;

    @QueryHandler
    public List<InventorySlot> handle(GetFacilityInventoryQuery q) {
        return availabilitySearchService.getInventorySlots(q.facilityId(), q.start(), q.end());
    }
}
