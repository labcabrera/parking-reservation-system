package org.labcabrera.parking.catalog.application.cqrs.handler;

import java.util.List;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;
import org.labcabrera.parking.catalog.application.cqrs.query.GetFacilityInventoryQuery;
import org.labcabrera.parking.catalog.application.service.FacilityAvailabilitySearchService;
import org.labcabrera.parking.catalog.interfaces.rest.dto.InventorySlotDto;

@Component
public class FacilityInventoryQueryHandler {

    private final FacilityAvailabilitySearchService availabilitySearchService;

    public FacilityInventoryQueryHandler(FacilityAvailabilitySearchService availabilitySearchService) {
        this.availabilitySearchService = availabilitySearchService;
    }

    @QueryHandler
    public List<InventorySlotDto> handle(GetFacilityInventoryQuery q) {
        return availabilitySearchService.getInventorySlots(q.facilityId(), q.start(), q.end()).stream()
            .map(s -> new InventorySlotDto(s.slotStart(), s.capacity(), s.reserved(), s.free()))
            .toList();
    }
}
