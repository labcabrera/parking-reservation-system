package org.labcabrera.parking.facilities.domain.event;

import java.util.UUID;

public record ReservationConfirmedEvent(UUID reservationId) {
}
