package org.labcabrera.parking.catalog.application.port.inbound;

import org.labcabrera.parking.catalog.application.cqrs.query.SearchParkingQuery;
import org.labcabrera.parking.catalog.application.dto.SearchResponse;

public interface SearchParkingPort {

    SearchResponse search(SearchParkingQuery query);
}
