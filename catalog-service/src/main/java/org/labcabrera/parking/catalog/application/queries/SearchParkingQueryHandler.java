package org.labcabrera.parking.catalog.application.queries;

import org.labcabrera.parking.catalog.application.dto.FacilityResult;
import org.labcabrera.parking.catalog.domain.model.AvailabilityWindow;
import org.labcabrera.parking.catalog.domain.model.FacilityStatus;
import org.labcabrera.parking.catalog.domain.model.FacilityTag;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.labcabrera.parking.catalog.domain.port.inbound.SearchParkingPort;
import org.labcabrera.parking.catalog.domain.port.outbound.AvailabilityCache;
import org.labcabrera.parking.catalog.domain.port.outbound.FacilityRepository;
import org.labcabrera.parking.catalog.domain.service.AvailabilityDomainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class SearchParkingQueryHandler implements SearchParkingPort {

    private final FacilityRepository facilityRepository;
    private final AvailabilityCache availabilityCache;
    private final AvailabilityDomainService availabilityDomainService;

    public SearchParkingQueryHandler(
            FacilityRepository facilityRepository,
            AvailabilityCache availabilityCache,
            AvailabilityDomainService availabilityDomainService) {
        this.facilityRepository = facilityRepository;
        this.availabilityCache = availabilityCache;
        this.availabilityDomainService = availabilityDomainService;
    }

    @Override
    public Page<FacilityResult> search(SearchParkingQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size());
        String text = query.text() != null ? query.text() : "";
        AvailabilityWindow window = new AvailabilityWindow(query.checkIn(), query.checkOut());

        return facilityRepository
                .findByTextAndStatus(text, FacilityStatus.ACTIVE, pageable)
                .map(facility -> toResult(facility, window));
    }

    private FacilityResult toResult(ParkingFacility facility, AvailabilityWindow window) {
        int availableSpots = availabilityCache
                .getAvailableSpotCount(facility.getId(), window)
                .orElseGet(() -> {
                    int count = facility.getTotalSpots();
                    availabilityCache.putAvailableSpotCount(facility.getId(), window, count);
                    return count;
                });

        boolean lowAvailability = availabilityDomainService
                .isLowAvailability(availableSpots, facility.getTotalSpots());

        return new FacilityResult(
                facility.getId().toString(),
                facility.getName(),
                facility.getCity(),
                facility.getAddress(),
                facility.getLocation().latitude(),
                facility.getLocation().longitude(),
                BigDecimal.ZERO,
                "EUR",
                facility.getTags().stream()
                        .map(FacilityTag::name)
                        .collect(Collectors.toSet()),
                lowAvailability,
                availableSpots);
    }
}
