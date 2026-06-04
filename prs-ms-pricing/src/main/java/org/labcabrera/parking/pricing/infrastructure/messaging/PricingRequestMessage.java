package org.labcabrera.parking.pricing.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PricingRequestMessage(
        UUID holdId,
        UUID facilityId,
        UUID spotId,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        BigDecimal baseRatePerDay,
        String currency) {
}
