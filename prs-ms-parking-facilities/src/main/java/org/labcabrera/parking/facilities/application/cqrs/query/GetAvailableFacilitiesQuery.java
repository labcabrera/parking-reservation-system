package org.labcabrera.parking.facilities.application.cqrs.query;

import java.time.LocalDateTime;

public record GetAvailableFacilitiesQuery(
    String text,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    Integer limit) {
}
