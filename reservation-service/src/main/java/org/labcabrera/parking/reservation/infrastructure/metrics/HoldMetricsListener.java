package org.labcabrera.parking.reservation.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.axonframework.eventhandling.annotations.EventHandler;
import org.labcabrera.parking.reservation.domain.model.events.HoldConvertedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldExpiredEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPricingFailedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldReleasedEvent;
import org.springframework.stereotype.Component;

/**
 * Micrometer counters for hold domain events.
 * Exposed via /actuator/prometheus.
 */
@Component
public class HoldMetricsListener {

    private final Counter holdCreatedCounter;
    private final Counter holdExpiredCounter;
    private final Counter holdPricingTimeoutCounter;
    private final Counter holdReleasedCounter;
    private final Counter holdConvertedCounter;

    public HoldMetricsListener(MeterRegistry meterRegistry) {
        this.holdCreatedCounter = Counter.builder("hold.created")
                .description("Number of holds created")
                .register(meterRegistry);
        this.holdExpiredCounter = Counter.builder("hold.expired")
                .description("Number of holds expired")
                .register(meterRegistry);
        this.holdPricingTimeoutCounter = Counter.builder("hold.pricing_timeout")
                .description("Number of holds where pricing failed/timed out")
                .register(meterRegistry);
        this.holdReleasedCounter = Counter.builder("hold.released")
                .description("Number of holds released by user")
                .register(meterRegistry);
        this.holdConvertedCounter = Counter.builder("hold.converted")
                .description("Number of holds converted to reservations")
                .register(meterRegistry);
    }

    @EventHandler
    public void onHoldCreated(HoldCreatedEvent event) {
        holdCreatedCounter.increment();
    }

    @EventHandler
    public void onHoldExpired(HoldExpiredEvent event) {
        holdExpiredCounter.increment();
    }

    @EventHandler
    public void onHoldPricingFailed(HoldPricingFailedEvent event) {
        holdPricingTimeoutCounter.increment();
    }

    @EventHandler
    public void onHoldReleased(HoldReleasedEvent event) {
        holdReleasedCounter.increment();
    }

    @EventHandler
    public void onHoldConverted(HoldConvertedEvent event) {
        holdConvertedCounter.increment();
    }
}
