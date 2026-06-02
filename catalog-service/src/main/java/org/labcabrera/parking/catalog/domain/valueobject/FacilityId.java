package org.labcabrera.parking.catalog.domain.valueobject;

import java.util.UUID;

public record FacilityId(UUID value) {

    public FacilityId {
        if (value == null) {
            throw new IllegalArgumentException("FacilityId value must not be null");
        }
    }

    public static FacilityId of(UUID value) {
        return new FacilityId(value);
    }

    public static FacilityId generate() {
        return new FacilityId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
