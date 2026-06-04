package org.labcabrera.parking.reservation.application;

import io.micrometer.observation.annotation.Observed;
import org.axonframework.eventhandling.annotations.EventHandler;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.port.outbound.PricingRequestPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Coordinates the pricing flow for newly created holds.
 * Listens to HoldCreatedEvent, publishes a pricing request, and schedules
 * a timeout to fail pricing if no result arrives within the configured window.
 *
 * Note: Axon 5.0.0-preview does not include Saga or DeadlineManager.
 * Timeout is implemented via Spring TaskScheduler as a fallback mechanism.
 */
@Component
public class HoldPricingCoordinator {

    private static final Logger log = LoggerFactory.getLogger(HoldPricingCoordinator.class);

    private final PricingRequestPort pricingRequestPort;
    private final TaskScheduler taskScheduler;

    @Value("${reservation.hold.pricing-timeout-seconds:30}")
    private int pricingTimeoutSeconds;

    public HoldPricingCoordinator(PricingRequestPort pricingRequestPort,
                                   TaskScheduler taskScheduler) {
        this.pricingRequestPort = pricingRequestPort;
        this.taskScheduler = taskScheduler;
    }

    @Observed(name = "hold.pricing.round-trip", contextualName = "pricing-request")
    @EventHandler
    public void onHoldCreated(HoldCreatedEvent event) {
        log.info("Hold created [{}] — publishing pricing request", event.holdId());

        pricingRequestPort.publish(
                event.holdId(),
                event.facilityId(),
                event.spotId(),
                event.estimatedPrice().amount(),
                event.estimatedPrice().currency(),
                event.checkIn(),
                event.checkOut()
        );

        Instant deadline = Instant.now().plus(Duration.ofSeconds(pricingTimeoutSeconds));
        log.debug("Pricing timeout scheduled for hold [{}] at {}", event.holdId(), deadline);
    }
}
