package org.labcabrera.parking.facilities.application.cqrs.command;

import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;

public record DeleteParkingFacilityCommand(

    FacilityId facilityId) {

}
