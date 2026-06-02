package org.labcabrera.parking.catalog.domain.port.outbound;

import java.util.Optional;

import org.labcabrera.parking.catalog.domain.valueobject.AvailabilityWindow;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityId;

public interface AvailabilityCache {

    Optional<Integer> getAvailableSpotCount(FacilityId facilityId, AvailabilityWindow window);

    void putAvailableSpotCount(FacilityId facilityId, AvailabilityWindow window, int count);

    void invalidate(String facilityId);
}
