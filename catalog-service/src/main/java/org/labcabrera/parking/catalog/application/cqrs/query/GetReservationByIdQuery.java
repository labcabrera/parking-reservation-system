package org.labcabrera.parking.catalog.application.cqrs.query;

import java.util.UUID;

public record GetReservationByIdQuery(UUID id) {
}
