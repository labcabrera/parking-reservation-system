package org.labcabrera.parking.catalog.domain.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.labcabrera.parking.catalog.application.cqrs.command.CancelReservationCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.ConfirmReservationCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.ExpireReservationCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.MarkReservationFailedCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.MarkReservationHeldCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.StartReservationCommand;
import org.labcabrera.parking.catalog.domain.event.ReservationCancelledEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationConfirmedEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationExpiredEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationFailedEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationHeldEvent;
import org.labcabrera.parking.catalog.domain.event.ReservationStartedEvent;
import org.labcabrera.parking.catalog.domain.valueobjects.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * State-stored Axon aggregate persisted via JPA. The 10-minute hold lifecycle and
 * inventory compensation are driven by {@code ReservationSaga}.
 */
@Aggregate(repository = "reservationRepository")
@Entity
@Table(name = "reservation", schema = "catalog")
@NoArgsConstructor
@Getter
public class Reservation {

    @Id
    @AggregateIdentifier
    private UUID id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "check_in", nullable = false)
    private LocalDateTime checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDateTime checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "estimated_price", precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Version
    private Long version;

    @CommandHandler
    public Reservation(StartReservationCommand cmd, ReservationConfig config) {
        LocalDateTime now = LocalDateTime.now();
        apply(new ReservationStartedEvent(
            cmd.reservationId(),
            cmd.facilityId(),
            cmd.userId(),
            cmd.checkIn(),
            cmd.checkOut(),
            now.plusMinutes(config.holdMinutes())));
    }

    @EventSourcingHandler
    void on(ReservationStartedEvent ev) {
        this.id = ev.reservationId();
        this.facilityId = ev.facilityId();
        this.userId = ev.userId();
        this.checkIn = ev.checkIn();
        this.checkOut = ev.checkOut();
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = ev.expiresAt();
    }

    @CommandHandler
    void handle(MarkReservationHeldCommand cmd) {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot mark as HELD from status " + status);
        }
        apply(new ReservationHeldEvent(cmd.reservationId(), cmd.estimatedPrice(), cmd.currency()));
    }

    @EventSourcingHandler
    void on(ReservationHeldEvent ev) {
        this.status = ReservationStatus.HELD;
        this.estimatedPrice = ev.estimatedPrice();
        this.currency = ev.currency();
    }

    @CommandHandler
    void handle(MarkReservationFailedCommand cmd) {
        if (status.isTerminal()) {
            return;
        }
        apply(new ReservationFailedEvent(cmd.reservationId(), cmd.reason()));
    }

    @EventSourcingHandler
    void on(ReservationFailedEvent ev) {
        this.status = ReservationStatus.FAILED;
        this.failureReason = ev.reason();
    }

    @CommandHandler
    void handle(ConfirmReservationCommand cmd) {
        if (status != ReservationStatus.HELD) {
            throw new IllegalStateException("Only HELD reservations can be confirmed (current: " + status + ")");
        }
        apply(new ReservationConfirmedEvent(cmd.reservationId()));
    }

    @EventSourcingHandler
    void on(ReservationConfirmedEvent ev) {
        this.status = ReservationStatus.CONFIRMED;
    }

    @CommandHandler
    void handle(CancelReservationCommand cmd) {
        if (status.isTerminal()) {
            return;
        }
        apply(new ReservationCancelledEvent(cmd.reservationId(), cmd.reason()));
    }

    @EventSourcingHandler
    void on(ReservationCancelledEvent ev) {
        this.status = ReservationStatus.CANCELLED;
        this.failureReason = ev.reason();
    }

    @CommandHandler
    void handle(ExpireReservationCommand cmd) {
        if (status == ReservationStatus.HELD || status == ReservationStatus.PENDING) {
            apply(new ReservationExpiredEvent(cmd.reservationId()));
        }
    }

    @EventSourcingHandler
    void on(ReservationExpiredEvent ev) {
        this.status = ReservationStatus.EXPIRED;
    }
}

