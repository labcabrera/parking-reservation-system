package org.labcabrera.parking.facilities.application.cqrs.handler;

import java.util.List;

import org.axonframework.queryhandling.QueryHandler;
import org.labcabrera.parking.facilities.application.cqrs.query.GetAvailableFacilitiesQuery;
import org.labcabrera.parking.facilities.application.service.FacilityAvailabilitySearchService;
import org.labcabrera.parking.facilities.interfaces.rest.dto.FacilityAvailabilityDto;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class FacilityAvailabilityQueryHandler {

    private final FacilityAvailabilitySearchService availabilitySearchService;

    @QueryHandler
    public List<FacilityAvailabilityDto> handle(GetAvailableFacilitiesQuery q) {
        log.info("Handling GetAvailableFacilitiesQuery: {}", q);
        return availabilitySearchService.search(q.text(), q.checkIn(), q.checkOut(), q.limit());
    }
}
