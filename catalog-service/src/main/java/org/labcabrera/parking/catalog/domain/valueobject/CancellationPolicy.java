package org.labcabrera.parking.catalog.domain.valueobject;

public record CancellationPolicy(int freeCancelHours, int penaltyCancelMinutes) {

    public CancellationPolicy {
        if (freeCancelHours < 0) {
            throw new IllegalArgumentException("freeCancelHours must be non-negative");
        }
        if (penaltyCancelMinutes < 0) {
            throw new IllegalArgumentException("penaltyCancelMinutes must be non-negative");
        }
    }
}
