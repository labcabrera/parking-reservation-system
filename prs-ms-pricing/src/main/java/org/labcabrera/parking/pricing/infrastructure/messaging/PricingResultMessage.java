package org.labcabrera.parking.pricing.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record PricingResultMessage(
        UUID holdId,
        BigDecimal confirmedPrice,
        String currency,
        String status,
        String failReason) {

    public static PricingResultMessage success(UUID holdId, BigDecimal confirmedPrice, String currency) {
        return new PricingResultMessage(holdId, confirmedPrice, currency, "SUCCESS", null);
    }

    public static PricingResultMessage failure(UUID holdId, String reason) {
        return new PricingResultMessage(holdId, null, null, "FAILED", reason);
    }
}
