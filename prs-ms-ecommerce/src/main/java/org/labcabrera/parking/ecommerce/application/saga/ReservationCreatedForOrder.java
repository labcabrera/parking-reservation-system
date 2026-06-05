package org.labcabrera.parking.ecommerce.application.saga;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationCreatedForOrder(
    UUID reservationId,
    BigDecimal amount,
    String currency) {
}
