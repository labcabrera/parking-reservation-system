package org.labcabrera.parking.pricing.domain.valueobject;

import java.math.BigDecimal;

public record SlotOccupancy(BigDecimal occupancyRate) {

    private static final BigDecimal MAX_OCCUPANCY_RATE = BigDecimal.ONE;

    public SlotOccupancy {
        if (occupancyRate == null) {
            throw new IllegalArgumentException("occupancyRate is required");
        }
        if (occupancyRate.signum() < 0) {
            throw new IllegalArgumentException("occupancyRate must be zero or greater");
        }
        if (occupancyRate.compareTo(MAX_OCCUPANCY_RATE) > 0) {
            throw new IllegalArgumentException("occupancyRate must be less than or equal to 1");
        }
    }
}
