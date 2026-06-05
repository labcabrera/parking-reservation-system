package org.labcabrera.parking.facilities.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationHeldEvent(UUID reservationId, BigDecimal estimatedPrice, String currency) {
}
