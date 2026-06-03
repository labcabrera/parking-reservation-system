package org.labcabrera.parking.catalog.application.cqrs.command;

import org.labcabrera.parking.catalog.domain.valueobject.FacilityId;

public record DeleteParkingFacilityCommand(

    FacilityId facilityId) {

}
