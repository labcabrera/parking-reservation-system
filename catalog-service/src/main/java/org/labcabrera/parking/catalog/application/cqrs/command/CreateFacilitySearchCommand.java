package org.labcabrera.parking.catalog.application.cqrs.command;

import java.time.LocalDateTime;
import java.util.Set;

public record CreateFacilitySearchCommand(
    String searchQuery,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    Set<String> features) {
}
