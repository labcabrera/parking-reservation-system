package org.labcabrera.parking.catalog.domain.valueobject;

import java.util.UUID;

public record SpotId(UUID value) {

    public SpotId {
        if (value == null) {
            throw new IllegalArgumentException("SpotId value must not be null");
        }
    }

    public static SpotId of(UUID value) {
        return new SpotId(value);
    }

    public static SpotId generate() {
        return new SpotId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
