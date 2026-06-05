package org.labcabrera.parking.ecommerce.domain.event;

import java.util.UUID;

public record OrderPaymentFailedEvent(UUID orderId, UUID paymentAttemptId, String reason) {
}
