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
        Set<String> tags,
        boolean lowAvailability,
        int availableSpots,
        BigDecimal estimatedPrice) {
}
