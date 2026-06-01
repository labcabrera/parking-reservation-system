package org.labcabrera.parking.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.labcabrera.parking.reservation.domain.model.Money;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.port.outbound.PricingRequestPort;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HoldPricingCoordinatorTest {

    @Mock
    private PricingRequestPort pricingRequestPort;

    @Mock
    private TaskScheduler taskScheduler;

    private HoldPricingCoordinator coordinator;

    private final UUID holdId = UUID.randomUUID();
    private final UUID spotId = UUID.randomUUID();
    private final UUID facilityId = UUID.randomUUID();
    private final Instant checkIn = Instant.now().plus(1, ChronoUnit.DAYS);
    private final Instant checkOut = Instant.now().plus(3, ChronoUnit.DAYS);
    private final Money estimatedPrice = new Money(new BigDecimal("50.00"), "EUR");

    @BeforeEach
    void setUp() {
        coordinator = new HoldPricingCoordinator(pricingRequestPort, taskScheduler);
    }

    @Test
    @DisplayName("onHoldCreated publishes a pricing request with correct holdId")
    void onHoldCreated_publishesPricingRequestWithHoldId() {
        var event = buildCreatedEvent();

        coordinator.onHoldCreated(event);

        verify(pricingRequestPort).publish(
                eq(holdId),
                eq(facilityId),
                eq(spotId),
                eq(estimatedPrice.amount()),
                eq(estimatedPrice.currency()),
                eq(checkIn),
                eq(checkOut)
        );
    }

    @Test
    @DisplayName("onHoldCreated publishes pricing request with baseRatePerDay from estimatedPrice")
    void onHoldCreated_usesEstimatedPriceAsBaseRate() {
        var event = buildCreatedEvent();

        coordinator.onHoldCreated(event);

        ArgumentCaptor<BigDecimal> rateCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(pricingRequestPort).publish(any(), any(), any(), rateCaptor.capture(), any(), any(), any());
        assertThat(rateCaptor.getValue()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("onHoldCreated publishes pricing request with correct currency")
    void onHoldCreated_usesCorrectCurrency() {
        var event = buildCreatedEvent();

        coordinator.onHoldCreated(event);

        ArgumentCaptor<String> currencyCaptor = ArgumentCaptor.forClass(String.class);
        verify(pricingRequestPort).publish(any(), any(), any(), any(), currencyCaptor.capture(), any(), any());
        assertThat(currencyCaptor.getValue()).isEqualTo("EUR");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private HoldCreatedEvent buildCreatedEvent() {
        return new HoldCreatedEvent(
                holdId,
                UUID.randomUUID().toString(),
                spotId,
                facilityId,
                "192.168.1.1",
                checkIn,
                checkOut,
                estimatedPrice,
                Instant.now().plus(10, ChronoUnit.MINUTES),
                Instant.now()
        );
    }
}
