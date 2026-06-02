package org.labcabrera.parking.catalog.application.saga;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.labcabrera.parking.catalog.application.cqrs.command.MarkReservationFailedCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.MarkReservationHeldCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.ExpireReservationCommand;
import org.labcabrera.parking.catalog.application.service.InventoryHoldService;
import org.labcabrera.parking.catalog.application.service.InventoryHoldService.HoldResult;
import org.labcabrera.parking.catalog.domain.event.ReservationCancelledEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationConfirmedEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationExpiredEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationStartedEvent;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * Coordinates the hold lifecycle of a reservation:
 * <ol>
 *   <li>On {@link ReservationStartedEvent} → attempt inventory hold via optimistic
 *       locking on every 30-min slot in range. On any per-slot failure, all previously
 *       held slots are compensated and the reservation is marked FAILED.</li>
 *   <li>On success → compute estimated price (stub; pricing-service async wiring
 *       TODO) and dispatch {@link MarkReservationHeldCommand}. Schedule a deadline
 *       at {@code expiresAt} that fires {@link ExpireReservationCommand}.</li>
 *   <li>On confirm / cancel / expire → release inventory (for cancel/expire) and
 *       end the saga.</li>
 * </ol>
 */
@Saga
@Slf4j
public class ReservationSaga {

    private static final String EXPIRY_DEADLINE = "reservation-expiry";

    private UUID facilityId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String expiryDeadlineId;

    @Autowired
    private transient InventoryHoldService inventoryHoldService;

    @Autowired
    private transient ParkingFacilityRepository facilityRepository;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @StartSaga
    @SagaEventHandler(associationProperty = "reservationId")
    public void on(ReservationStartedEvent ev) {
        log.info("Received ReservationStartedEvent for reservation {} on facility {} from {} to {}, expires at {}",
            ev.reservationId(), ev.facilityId(), ev.checkIn(), ev.checkOut(), ev.expiresAt());
            
        this.facilityId = ev.facilityId();
        this.checkIn = ev.checkIn();
        this.checkOut = ev.checkOut();

        log.info("Saga started for reservation {} on facility {} from {} to {}",
            ev.reservationId(), facilityId, checkIn, checkOut);

        HoldResult result = inventoryHoldService.tryHold(facilityId, checkIn, checkOut);
        if (!result.success()) {
            commandGateway.send(new MarkReservationFailedCommand(ev.reservationId(), result.failureReason()));
            return;
        }

        BigDecimal estimated = estimatePrice(facilityId, checkIn, checkOut);
        commandGateway.send(new MarkReservationHeldCommand(ev.reservationId(), estimated, "EUR"));

        Duration ttl = Duration.between(LocalDateTime.now(), ev.expiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofSeconds(1);
        }
        this.expiryDeadlineId = deadlineManager.schedule(
            ttl,
            EXPIRY_DEADLINE,
            ev.reservationId());
        log.debug("Scheduled expiry for reservation {} in {}", ev.reservationId(), ttl);
    }

    @DeadlineHandler(deadlineName = EXPIRY_DEADLINE)
    public void onExpiry(UUID reservationId) {
        log.info("Hold expired for reservation {}; firing ExpireReservationCommand", reservationId);
        commandGateway.send(new ExpireReservationCommand(reservationId));
    }

    @SagaEventHandler(associationProperty = "reservationId")
    @EndSaga
    public void on(ReservationConfirmedEvent ev) {
        log.info("Reservation {} confirmed; saga ending", ev.reservationId());
        cancelExpiryDeadline();
        // Inventory stays reserved (confirmed reservation owns it).
    }

    @SagaEventHandler(associationProperty = "reservationId")
    @EndSaga
    public void on(ReservationCancelledEvent ev) {
        log.info("Reservation {} cancelled; releasing inventory", ev.reservationId());
        cancelExpiryDeadline();
        releaseInventory();
    }

    @SagaEventHandler(associationProperty = "reservationId")
    @EndSaga
    public void on(ReservationExpiredEvent ev) {
        log.info("Reservation {} expired; releasing inventory", ev.reservationId());
        releaseInventory();
    }

    private void releaseInventory() {
        if (facilityId != null && checkIn != null && checkOut != null) {
            inventoryHoldService.release(facilityId, checkIn, checkOut);
        }
    }

    private void cancelExpiryDeadline() {
        if (expiryDeadlineId != null) {
            try {
                deadlineManager.cancelSchedule(EXPIRY_DEADLINE, expiryDeadlineId);
            }
            catch (Exception e) {
                log.debug("Could not cancel expiry deadline {}: {}", expiryDeadlineId, e.getMessage());
            }
        }
        SagaLifecycle.end();
    }

    /**
     * TODO replace with async pricing-service call (Kafka request/response).
     * Uses facility's estimatedDailyPrice prorated by duration as a placeholder.
     */
    private BigDecimal estimatePrice(UUID facilityId, LocalDateTime in, LocalDateTime out) {
        return facilityRepository.findById(new FacilityId(facilityId))
            .map(f -> {
                BigDecimal daily = f.getPricingRule().estimatedDailyPrice();
                long minutes = Duration.between(in, out).toMinutes();
                return daily
                    .multiply(BigDecimal.valueOf(minutes))
                    .divide(BigDecimal.valueOf(24L * 60L), 2, RoundingMode.HALF_UP);
            })
            .orElse(BigDecimal.ZERO);
    }
}
