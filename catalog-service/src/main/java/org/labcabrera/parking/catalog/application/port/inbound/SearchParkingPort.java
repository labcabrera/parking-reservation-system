package org.labcabrera.parking.catalog.application.port.inbound;

import org.labcabrera.parking.catalog.application.dto.SearchResponse;
import org.labcabrera.parking.catalog.application.queries.SearchParkingQuery;

public interface SearchParkingPort {

    SearchResponse search(SearchParkingQuery query);
}
