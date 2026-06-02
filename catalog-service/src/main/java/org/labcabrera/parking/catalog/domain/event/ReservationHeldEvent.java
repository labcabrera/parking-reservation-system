package org.labcabrera.parking.catalog.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationHeldEvent(UUID reservationId, BigDecimal estimatedPrice, String currency) {
}
