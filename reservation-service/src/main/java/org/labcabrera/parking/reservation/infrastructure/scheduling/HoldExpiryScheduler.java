package org.labcabrera.parking.reservation.infrastructure.scheduling;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.reservation.application.commands.ExpireHoldCommand;
import org.labcabrera.parking.reservation.domain.model.HoldReadModel;
import org.labcabrera.parking.reservation.domain.model.HoldStatus;
import org.labcabrera.parking.reservation.domain.port.outbound.HoldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Fallback sweep scheduler that issues ExpireHoldCommand for all PENDING_PRICE or ACTIVE
 * holds whose expiresAt is in the past. Guards against missed Axon deadline delivery.
 * Runs every 5 minutes.
 */
@Component
public class HoldExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpiryScheduler.class);

    private final HoldRepository holdRepository;
    private final CommandGateway commandGateway;

    public HoldExpiryScheduler(HoldRepository holdRepository, CommandGateway commandGateway) {
        this.holdRepository = holdRepository;
        this.commandGateway = commandGateway;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void expireOverdueHolds() {
        Instant now = Instant.now();
        List<HoldReadModel> pendingExpiry = holdRepository.findByStatusAndExpiresAtBefore(HoldStatus.PENDING_PRICE, now);
        List<HoldReadModel> activeExpiry = holdRepository.findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, now);

        int count = 0;
        for (HoldReadModel hold : pendingExpiry) {
            commandGateway.sendAndWait(new ExpireHoldCommand(hold.holdId()));
            count++;
        }
        for (HoldReadModel hold : activeExpiry) {
            commandGateway.sendAndWait(new ExpireHoldCommand(hold.holdId()));
            count++;
        }

        if (count > 0) {
            log.info("HoldExpiryScheduler expired {} overdue holds", count);
        }
    }
}
