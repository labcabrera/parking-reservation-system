package org.labcabrera.parking.catalog.domain.event;

import java.util.UUID;

public record ReservationFailedEvent(UUID reservationId, String reason) {
}
