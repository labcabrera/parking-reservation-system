package org.labcabrera.parking.ecommerce.domain.event;

import java.util.UUID;

public record OrderPaidEvent(UUID orderId, UUID paymentAttemptId) {
}
