package org.labcabrera.parking.catalog.domain.port.outbound;

import org.labcabrera.parking.catalog.domain.valueobjects.AvailabilityWindow;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;

import java.util.Optional;

public interface AvailabilityCache {

    Optional<Integer> getAvailableSpotCount(FacilityId facilityId, AvailabilityWindow window);

    void putAvailableSpotCount(FacilityId facilityId, AvailabilityWindow window, int count);

    void invalidate(String facilityId);
}
