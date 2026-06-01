package org.labcabrera.parking.catalog.application.port.inbound;

import org.labcabrera.parking.catalog.application.dto.FacilityResult;
import org.labcabrera.parking.catalog.application.queries.SearchParkingQuery;
import org.springframework.data.domain.Page;

public interface SearchParkingPort {

    Page<FacilityResult> search(SearchParkingQuery query);
}
