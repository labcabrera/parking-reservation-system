package org.labcabrera.parking.catalog.application.cqrs.handler;

import java.util.List;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

import org.labcabrera.parking.catalog.application.cqrs.query.GetAvailableFacilitiesQuery;
import org.labcabrera.parking.catalog.application.service.FacilityAvailabilitySearchService;
import org.labcabrera.parking.catalog.interfaces.rest.dto.FacilityAvailabilityDto;

@Component
@AllArgsConstructor
public class FacilityAvailabilityQueryHandler {

    private final FacilityAvailabilitySearchService availabilitySearchService;

    @QueryHandler
    public List<FacilityAvailabilityDto> handle(GetAvailableFacilitiesQuery q) {
        return availabilitySearchService.search(q.text(), q.checkIn(), q.checkOut(), q.limit());
    }
}
