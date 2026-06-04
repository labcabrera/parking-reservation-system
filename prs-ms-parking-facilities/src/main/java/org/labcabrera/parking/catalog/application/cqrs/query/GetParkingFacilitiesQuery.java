package org.labcabrera.parking.catalog.application.cqrs.query;

import org.springframework.data.domain.Pageable;

public record GetParkingFacilitiesQuery(
    String rsql,
    Pageable pageable) {

}
