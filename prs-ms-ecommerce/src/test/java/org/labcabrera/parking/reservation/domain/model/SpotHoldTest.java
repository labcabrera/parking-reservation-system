package org.labcabrera.parking.reservation.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.labcabrera.parking.reservation.domain.model.events.HoldConvertedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldExpiredEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPriceConfirmedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPricingFailedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldReleasedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpotHoldTest {

    private final UUID holdId = UUID.randomUUID();
    private final UUID spotId = UUID.randomUUID();
    private final UUID facilityId = UUID.randomUUID();
    private final String sessionId = UUID.randomUUID().toString();
    private final String visitorIp = "192.168.1.1";
    private final Instant checkIn = Instant.now().plus(1, ChronoUnit.DAYS);
    private final Instant checkOut = Instant.now().plus(3, ChronoUnit.DAYS);
    private final Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
    private final Money estimatedPrice = new Money(new BigDecimal("60.00"), "EUR");
    private final Money confirmedPrice = new Money(new BigDecimal("62.00"), "EUR");

    private SpotHold hold;

    @BeforeEach
    void setUp() {
        hold = new SpotHold(holdId);
    }

    @Test
    @DisplayName("HoldCreatedEvent sets status to PENDING_PRICE and populates fields")
    void onHoldCreatedEvent_setsStatusToPendingPrice() {
        var event = new HoldCreatedEvent(holdId, sessionId, spotId, facilityId,
                visitorIp, checkIn, checkOut, estimatedPrice, expiresAt, Instant.now());

        hold.on(event);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.PENDING_PRICE);
        assertThat(hold.isPendingPrice()).isTrue();
        assertThat(hold.getSpotId()).isEqualTo(spotId);
        assertThat(hold.getFacilityId()).isEqualTo(facilityId);
        assertThat(hold.getEstimatedPrice()).isEqualTo(estimatedPrice);
    }

    @Test
    @DisplayName("HoldPriceConfirmedEvent sets status to ACTIVE and records confirmed price")
    void onHoldPriceConfirmedEvent_setsStatusToActive() {
        applyCreatedEvent();
        var event = new HoldPriceConfirmedEvent(holdId, confirmedPrice, Instant.now());

        hold.on(event);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(hold.isActive()).isTrue();
        assertThat(hold.getConfirmedPrice()).isEqualTo(confirmedPrice);
        assertThat(hold.canConvert()).isTrue();
    }

    @Test
    @DisplayName("HoldPricingFailedEvent sets status to FAILED")
    void onHoldPricingFailedEvent_setsStatusToFailed() {
        applyCreatedEvent();
        var event = new HoldPricingFailedEvent(holdId, "Pricing service timeout", Instant.now());

        hold.on(event);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.FAILED);
        assertThat(hold.canRelease()).isFalse();
        assertThat(hold.canConvert()).isFalse();
    }

    @Test
    @DisplayName("HoldReleasedEvent sets status to RELEASED")
    void onHoldReleasedEvent_setsStatusToReleased() {
        applyCreatedEvent();
        var event = new HoldReleasedEvent(holdId, facilityId, spotId, checkIn, checkOut, Instant.now());

        hold.on(event);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.RELEASED);
        assertThat(hold.canRelease()).isFalse();
    }

    @Test
    @DisplayName("HoldExpiredEvent sets status to EXPIRED")
    void onHoldExpiredEvent_setsStatusToExpired() {
        applyCreatedEvent();
        var event = new HoldExpiredEvent(holdId, facilityId, spotId, checkIn, checkOut, Instant.now());

        hold.on(event);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(hold.canExpire()).isFalse();
    }

    @Test
    @DisplayName("HoldConvertedEvent sets status to CONVERTED")
    void onHoldConvertedEvent_setsStatusToConverted() {
        applyCreatedEvent();
        applyConfirmedEvent();
        var reservationId = UUID.randomUUID();
        var event = new HoldConvertedEvent(holdId, reservationId, Instant.now());

        hold.on(event);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CONVERTED);
        assertThat(hold.canConvert()).isFalse();
    }

    @Test
    @DisplayName("PENDING_PRICE hold can be released")
    void pendingPriceHold_canBeReleased() {
        applyCreatedEvent();
        assertThat(hold.canRelease()).isTrue();
    }

    @Test
    @DisplayName("ACTIVE hold can be released")
    void activeHold_canBeReleased() {
        applyCreatedEvent();
        applyConfirmedEvent();
        assertThat(hold.canRelease()).isTrue();
    }

    @Test
    @DisplayName("PENDING_PRICE hold can expire")
    void pendingPriceHold_canExpire() {
        applyCreatedEvent();
        assertThat(hold.canExpire()).isTrue();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private void applyCreatedEvent() {
        hold.on(new HoldCreatedEvent(holdId, sessionId, spotId, facilityId,
                visitorIp, checkIn, checkOut, estimatedPrice, expiresAt, Instant.now()));
    }

    private void applyConfirmedEvent() {
        hold.on(new HoldPriceConfirmedEvent(holdId, confirmedPrice, Instant.now()));
    }
}
