package org.labcabrera.parking.catalog.application.cqrs.query;

import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;

public record GetParkingFacilityByIdQuery(
    FacilityId facilityId) {
}
