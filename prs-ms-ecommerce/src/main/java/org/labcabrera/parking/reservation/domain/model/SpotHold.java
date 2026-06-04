package org.labcabrera.parking.reservation.domain.model;

import org.axonframework.eventsourcing.annotations.EventSourcedEntity;
import org.axonframework.eventsourcing.annotations.EventSourcingHandler;
import org.axonframework.eventsourcing.annotations.reflection.EntityCreator;
import org.axonframework.eventsourcing.annotations.reflection.InjectEntityId;
import org.labcabrera.parking.reservation.domain.model.events.HoldConvertedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldExpiredEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPriceConfirmedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPricingFailedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldReleasedEvent;

import java.time.Instant;
import java.util.UUID;

@EventSourcedEntity
public class SpotHold {

    private UUID holdId;
    private String searchSessionId;
    private UUID spotId;
    private UUID facilityId;
    private String visitorIp;
    private Instant checkIn;
    private Instant checkOut;
    private Money estimatedPrice;
    private Money confirmedPrice;
    private HoldStatus status;
    private Instant expiresAt;

    @EntityCreator
    public SpotHold(@InjectEntityId UUID holdId) {
        this.holdId = holdId;
    }

    // ─── Business state checks ─────────────────────────────────────────────

    public boolean isPendingPrice() {
        return HoldStatus.PENDING_PRICE == status;
    }

    public boolean isActive() {
        return HoldStatus.ACTIVE == status;
    }

    public boolean canRelease() {
        return status == HoldStatus.PENDING_PRICE || status == HoldStatus.ACTIVE;
    }

    public boolean canExpire() {
        return status == HoldStatus.PENDING_PRICE || status == HoldStatus.ACTIVE;
    }

    public boolean canConvert() {
        return status == HoldStatus.ACTIVE;
    }

    // ─── Event Sourcing Handlers (state mutations) ─────────────────────────

    @EventSourcingHandler
    public void on(HoldCreatedEvent event) {
        this.searchSessionId = event.searchSessionId();
        this.spotId = event.spotId();
        this.facilityId = event.facilityId();
        this.visitorIp = event.visitorIp();
        this.checkIn = event.checkIn();
        this.checkOut = event.checkOut();
        this.estimatedPrice = event.estimatedPrice();
        this.expiresAt = event.expiresAt();
        this.status = HoldStatus.PENDING_PRICE;
    }

    @EventSourcingHandler
    public void on(HoldPriceConfirmedEvent event) {
        this.confirmedPrice = event.confirmedPrice();
        this.status = HoldStatus.ACTIVE;
    }

    @EventSourcingHandler
    public void on(HoldPricingFailedEvent event) {
        this.status = HoldStatus.FAILED;
    }

    @EventSourcingHandler
    public void on(HoldReleasedEvent event) {
        this.status = HoldStatus.RELEASED;
    }

    @EventSourcingHandler
    public void on(HoldExpiredEvent event) {
        this.status = HoldStatus.EXPIRED;
    }

    @EventSourcingHandler
    public void on(HoldConvertedEvent event) {
        this.status = HoldStatus.CONVERTED;
    }

    // ─── Getters ──────────────────────────────────────────────────────────

    public UUID getHoldId() { return holdId; }
    public String getSearchSessionId() { return searchSessionId; }
    public UUID getSpotId() { return spotId; }
    public UUID getFacilityId() { return facilityId; }
    public String getVisitorIp() { return visitorIp; }
    public Instant getCheckIn() { return checkIn; }
    public Instant getCheckOut() { return checkOut; }
    public Money getEstimatedPrice() { return estimatedPrice; }
    public Money getConfirmedPrice() { return confirmedPrice; }
    public HoldStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
}
