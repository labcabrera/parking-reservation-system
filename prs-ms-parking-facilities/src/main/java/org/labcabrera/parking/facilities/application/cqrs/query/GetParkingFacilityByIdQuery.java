package org.labcabrera.parking.facilities.application.cqrs.query;

import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;

public record GetParkingFacilityByIdQuery(
    FacilityId facilityId) {
}
