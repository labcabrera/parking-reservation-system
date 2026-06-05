package org.labcabrera.parking.ecommerce.domain.event;

import java.util.UUID;

public record OrderPaymentInProgressEvent(UUID orderId, UUID paymentAttemptId) {
}
