package org.labcabrera.parking.catalog.application.cqrs.query;

import java.time.LocalDateTime;
import java.util.List;

public record SearchParkingQuery(
    String text,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    int page,
    int size,
    Double lat,
    Double lng,
    Double radiusKm,
    List<String> features) {

    public static SearchParkingQuery of(String text, LocalDateTime checkIn, LocalDateTime checkOut) {
        return new SearchParkingQuery(text, checkIn, checkOut, 0, 20, null, null, null, null);
    }

    public static SearchParkingQuery of(
        String text, LocalDateTime checkIn, LocalDateTime checkOut, int page, int size) {
        return new SearchParkingQuery(text, checkIn, checkOut, page, size, null, null, null, null);
    }

    public static SearchParkingQuery of(
        String text, LocalDateTime checkIn, LocalDateTime checkOut, int page, int size,
        Double lat, Double lng, Double radiusKm, List<String> features) {
        return new SearchParkingQuery(text, checkIn, checkOut, page, size, lat, lng, radiusKm, features);
    }
}
