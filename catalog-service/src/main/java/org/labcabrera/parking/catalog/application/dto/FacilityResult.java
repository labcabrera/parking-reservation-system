package org.labcabrera.parking.catalog.application.dto;

import java.math.BigDecimal;
import java.util.Set;

public record FacilityResult(
        String facilityId,
        String name,
        String city,
        String address,
        double latitude,
        double longitude,
        BigDecimal dailyRate,
        String currency,
        Set<String> tags,
        boolean lowAvailabilityWarning,
        int availableSpots) {
}
