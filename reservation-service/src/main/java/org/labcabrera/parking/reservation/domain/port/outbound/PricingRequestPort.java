package org.labcabrera.parking.reservation.domain.port.outbound;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbound port for publishing pricing requests to the pricing service.
 */
public interface PricingRequestPort {

    void publish(UUID holdId, UUID facilityId, UUID spotId,
                 BigDecimal baseRatePerDay, String currency,
                 Instant checkIn, Instant checkOut);
}
