package org.labcabrera.parking.facilities.domain.event;

import java.util.UUID;

public record ReservationFailedEvent(UUID reservationId, String reason) {
}
