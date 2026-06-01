package org.labcabrera.parking.catalog.application.queries;

import java.time.LocalDateTime;

public record SearchParkingQuery(
        String text,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        int page,
        int size) {

    public static SearchParkingQuery of(String text, LocalDateTime checkIn, LocalDateTime checkOut) {
        return new SearchParkingQuery(text, checkIn, checkOut, 0, 20);
    }

    public static SearchParkingQuery of(
            String text, LocalDateTime checkIn, LocalDateTime checkOut, int page, int size) {
        return new SearchParkingQuery(text, checkIn, checkOut, page, size);
    }
}
