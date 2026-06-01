package org.labcabrera.parking.catalog.domain.model;

public record Coordinates(double latitude, double longitude) {

    public Coordinates {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got: " + longitude);
        }
    }
}
