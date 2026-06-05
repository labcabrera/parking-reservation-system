package org.labcabrera.parking.facilities.application.cqrs.query;

import org.springframework.data.domain.Pageable;

public record GetParkingFacilitiesQuery(
    String rsql,
    Pageable pageable) {

}
