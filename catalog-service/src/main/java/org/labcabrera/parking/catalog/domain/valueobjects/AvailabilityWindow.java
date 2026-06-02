package org.labcabrera.parking.catalog.domain.valueobjects;

import java.time.LocalDateTime;

public record AvailabilityWindow(LocalDateTime checkIn, LocalDateTime checkOut) {

    public AvailabilityWindow {
        if (checkIn == null) {
            throw new IllegalArgumentException("checkIn must not be null");
        }
        if (checkOut == null) {
            throw new IllegalArgumentException("checkOut must not be null");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }
}
