package org.labcabrera.parking.ecommerce.application.saga;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.labcabrera.parking.ecommerce.application.cqrs.command.CreateOrderCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.command.ExpireOrderCommand;
import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.event.OrderExpiredEvent;
import org.labcabrera.parking.ecommerce.domain.event.OrderPaidEvent;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Saga
@Slf4j
public class OrderSaga {

    private static final String PAYMENT_EXPIRY_DEADLINE = "order-payment-expiry";

    private UUID reservationId;
    private UUID orderId;
    private String paymentExpiryDeadlineId;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient OrderReadRepository orderReadRepository;

    @StartSaga
    @SagaEventHandler(associationProperty = "reservationId")
    public void on(ReservationCreatedForOrder event) {
        this.reservationId = event.reservationId();

        if (event.expiresAt() == null) {
            log.error("Cannot create ecommerce order for reservation {} because expiresAt is missing", event.reservationId());
            SagaLifecycle.end();
            return;
        }

        orderReadRepository.findByHoldId(event.reservationId()).ifPresentOrElse(existing -> {
            log.info("Order {} already exists for reservation {}; ending duplicate saga",
                existing.getId(), event.reservationId());
            SagaLifecycle.end();
        }, () -> createOrder(event));
    }

    private void createOrder(ReservationCreatedForOrder event) {
        UUID newOrderId = UUID.randomUUID();
        this.orderId = newOrderId;
        SagaLifecycle.associateWith("orderId", newOrderId.toString());

        log.info("Creating order {} from reservation {} expires at {}",
            newOrderId, event.reservationId(), event.expiresAt());
        commandGateway.sendAndWait(new CreateOrderCommand(
            newOrderId,
            event.reservationId(),
            event.bookingSessionId(),
            event.expiresAt(),
            event.amount(),
            event.currency()), 10, TimeUnit.SECONDS);

        schedulePaymentExpiry(event.expiresAt());
    }

    private void schedulePaymentExpiry(LocalDateTime expiresAt) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofSeconds(1);
        }
        this.paymentExpiryDeadlineId = deadlineManager.schedule(ttl, PAYMENT_EXPIRY_DEADLINE, orderId);
        log.debug("Scheduled payment expiry for order {} from reservation {} in {}", orderId, reservationId, ttl);
    }

    @DeadlineHandler(deadlineName = PAYMENT_EXPIRY_DEADLINE)
    public void onPaymentExpiry(UUID deadlineOrderId) {
        if (orderId == null || !orderId.equals(deadlineOrderId)) {
            log.warn("Ignoring payment expiry deadline for order {} in saga for order {}", deadlineOrderId, orderId);
            return;
        }
        log.info("Payment window expired for order {}; firing ExpireOrderCommand", orderId);
        commandGateway.send(new ExpireOrderCommand(orderId));
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void on(OrderPaidEvent event) {
        log.info("Order {} paid; ending order saga", event.orderId());
        cancelPaymentExpiryDeadline();
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void on(OrderExpiredEvent event) {
        log.info("Order {} expired; ending order saga", event.orderId());
        cancelPaymentExpiryDeadline();
    }

    private void cancelPaymentExpiryDeadline() {
        if (paymentExpiryDeadlineId != null) {
            try {
                deadlineManager.cancelSchedule(PAYMENT_EXPIRY_DEADLINE, paymentExpiryDeadlineId);
            }
            catch (Exception e) {
                log.debug("Could not cancel payment expiry deadline {}: {}", paymentExpiryDeadlineId, e.getMessage());
            }
        }
    }
}
