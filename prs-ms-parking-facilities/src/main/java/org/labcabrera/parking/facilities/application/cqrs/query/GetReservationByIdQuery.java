package org.labcabrera.parking.facilities.application.cqrs.query;

import java.util.UUID;

public record GetReservationByIdQuery(UUID id) {
}
