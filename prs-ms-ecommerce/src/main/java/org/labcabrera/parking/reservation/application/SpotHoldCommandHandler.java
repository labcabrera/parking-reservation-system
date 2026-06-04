package org.labcabrera.parking.reservation.application;

import org.axonframework.commandhandling.annotations.CommandHandler;
import org.axonframework.eventhandling.EventSink;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.messaging.MessageType;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.modelling.StateManager;
import org.labcabrera.parking.reservation.application.commands.ConfirmHoldPriceCommand;
import org.labcabrera.parking.reservation.application.commands.ConvertHoldCommand;
import org.labcabrera.parking.reservation.application.commands.CreateHoldCommand;
import org.labcabrera.parking.reservation.application.commands.ExpireHoldCommand;
import org.labcabrera.parking.reservation.application.commands.FailHoldPricingCommand;
import org.labcabrera.parking.reservation.application.commands.ReleaseHoldCommand;
import org.labcabrera.parking.reservation.domain.model.SpotHold;
import org.labcabrera.parking.reservation.domain.model.events.HoldConvertedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldCreatedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldExpiredEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPriceConfirmedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldPricingFailedEvent;
import org.labcabrera.parking.reservation.domain.model.events.HoldReleasedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SpotHoldCommandHandler {

    private final StateManager stateManager;
    private final EventSink eventSink;

    public SpotHoldCommandHandler(StateManager stateManager, EventSink eventSink) {
        this.stateManager = stateManager;
        this.eventSink = eventSink;
    }

    @CommandHandler
    public void handle(CreateHoldCommand cmd, ProcessingContext ctx) {
        var event = new HoldCreatedEvent(
                cmd.holdId(), cmd.searchSessionId(), cmd.spotId(), cmd.facilityId(),
                cmd.visitorIp(), cmd.checkIn(), cmd.checkOut(), cmd.estimatedPrice(),
                cmd.expiresAt(), Instant.now());
        eventSink.publish(ctx, new GenericEventMessage(new MessageType(HoldCreatedEvent.class), event));
    }

    @CommandHandler
    public void handle(ConfirmHoldPriceCommand cmd, ProcessingContext ctx) {
        SpotHold hold = stateManager.loadEntity(SpotHold.class, cmd.holdId(), ctx).join();
        if (!hold.isPendingPrice()) {
            throw new IllegalStateException(
                    "Cannot confirm price: hold " + cmd.holdId() + " is not in PENDING_PRICE state");
        }
        var event = new HoldPriceConfirmedEvent(cmd.holdId(), cmd.confirmedPrice(), Instant.now());
        eventSink.publish(ctx, new GenericEventMessage(new MessageType(HoldPriceConfirmedEvent.class), event));
    }

    @CommandHandler
    public void handle(FailHoldPricingCommand cmd, ProcessingContext ctx) {
        SpotHold hold = stateManager.loadEntity(SpotHold.class, cmd.holdId(), ctx).join();
        if (!hold.isPendingPrice()) {
            throw new IllegalStateException(
                    "Cannot fail pricing: hold " + cmd.holdId() + " is not in PENDING_PRICE state");
        }
        var event = new HoldPricingFailedEvent(cmd.holdId(), cmd.reason(), Instant.now());
        eventSink.publish(ctx, new GenericEventMessage(new MessageType(HoldPricingFailedEvent.class), event));
    }

    @CommandHandler
    public void handle(ReleaseHoldCommand cmd, ProcessingContext ctx) {
        SpotHold hold = stateManager.loadEntity(SpotHold.class, cmd.holdId(), ctx).join();
        if (!hold.canRelease()) {
            throw new IllegalStateException(
                    "Cannot release hold " + cmd.holdId() + " in status " + hold.getStatus());
        }
        var event = new HoldReleasedEvent(
                cmd.holdId(), hold.getFacilityId(), hold.getSpotId(),
                hold.getCheckIn(), hold.getCheckOut(), Instant.now());
        eventSink.publish(ctx, new GenericEventMessage(new MessageType(HoldReleasedEvent.class), event));
    }

    @CommandHandler
    public void handle(ExpireHoldCommand cmd, ProcessingContext ctx) {
        SpotHold hold = stateManager.loadEntity(SpotHold.class, cmd.holdId(), ctx).join();
        if (!hold.canExpire()) {
            throw new IllegalStateException(
                    "Cannot expire hold " + cmd.holdId() + " in status " + hold.getStatus());
        }
        var event = new HoldExpiredEvent(
                cmd.holdId(), hold.getFacilityId(), hold.getSpotId(),
                hold.getCheckIn(), hold.getCheckOut(), Instant.now());
        eventSink.publish(ctx, new GenericEventMessage(new MessageType(HoldExpiredEvent.class), event));
    }

    @CommandHandler
    public void handle(ConvertHoldCommand cmd, ProcessingContext ctx) {
        SpotHold hold = stateManager.loadEntity(SpotHold.class, cmd.holdId(), ctx).join();
        if (!hold.canConvert()) {
            throw new IllegalStateException(
                    "Cannot convert hold " + cmd.holdId() + " in status " + hold.getStatus());
        }
        var event = new HoldConvertedEvent(cmd.holdId(), cmd.reservationId(), Instant.now());
        eventSink.publish(ctx, new GenericEventMessage(new MessageType(HoldConvertedEvent.class), event));
    }
}
