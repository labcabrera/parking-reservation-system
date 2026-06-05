package org.labcabrera.parking.facilities.application.cqrs.handler;

import java.util.List;
import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.facilities.application.cqrs.query.GetFacilityInventoryQuery;
import org.labcabrera.parking.facilities.application.service.FacilityAvailabilitySearchService;
import org.labcabrera.parking.facilities.domain.valueobject.InventorySlot;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class FacilityInventoryQueryHandler {

    private final FacilityAvailabilitySearchService availabilitySearchService;

    @QueryHandler
    public List<InventorySlot> handle(GetFacilityInventoryQuery q) {
        return availabilitySearchService.getInventorySlots(q.facilityId(), q.start(), q.end());
    }
}
