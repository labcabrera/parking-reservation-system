package org.labcabrera.parking.facilities.application.cqrs.command;

import java.time.LocalDateTime;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record StartReservationCommand(
    @TargetAggregateIdentifier UUID reservationId,
    UUID facilityId,
    String userId,
    LocalDateTime checkIn,
    LocalDateTime checkOut) {

    public StartReservationCommand {
        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is required");
        }
        if (facilityId == null) {
            throw new IllegalArgumentException("facilityId is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }
}
