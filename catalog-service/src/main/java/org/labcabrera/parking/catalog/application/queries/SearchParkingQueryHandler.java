package org.labcabrera.parking.catalog.application.queries;

import org.labcabrera.parking.catalog.application.dto.FacilityResult;
import org.labcabrera.parking.catalog.application.dto.SearchResponse;
import org.labcabrera.parking.catalog.domain.model.AvailabilityWindow;
import org.labcabrera.parking.catalog.domain.model.FacilityStatus;
import org.labcabrera.parking.catalog.domain.model.FacilityTag;
import org.labcabrera.parking.catalog.domain.model.Money;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.labcabrera.parking.catalog.application.port.inbound.SearchParkingPort;
import org.labcabrera.parking.catalog.domain.port.outbound.AvailabilityCache;
import org.labcabrera.parking.catalog.domain.port.outbound.FacilityRepository;
import org.labcabrera.parking.catalog.domain.service.AvailabilityDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SearchParkingQueryHandler implements SearchParkingPort {

    private static final Logger log = LoggerFactory.getLogger(SearchParkingQueryHandler.class);

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final FacilityRepository facilityRepository;
    private final AvailabilityCache availabilityCache;
    private final AvailabilityDomainService availabilityDomainService;

    @Value("${catalog.search.max-page-size:100}")
    private int maxPageSize;

    @Value("${catalog.search.low-availability-threshold:5}")
    private int lowAvailabilityThreshold;

    public SearchParkingQueryHandler(
            FacilityRepository facilityRepository,
            AvailabilityCache availabilityCache,
            AvailabilityDomainService availabilityDomainService) {
        this.facilityRepository = facilityRepository;
        this.availabilityCache = availabilityCache;
        this.availabilityDomainService = availabilityDomainService;
    }

    @Override
    public SearchResponse search(SearchParkingQuery query) {
        int pageSize = Math.min(query.size(), maxPageSize);
        PageRequest pageable = PageRequest.of(query.page(), pageSize);
        String text = query.text() != null ? query.text() : "";
        AvailabilityWindow window = new AvailabilityWindow(query.checkIn(), query.checkOut());

        String searchSessionId = UUID.randomUUID().toString();
        boolean stale = false;

        Page<ParkingFacility> facilityPage;
        try {
            facilityPage = facilityRepository.findByTextAndStatus(text, FacilityStatus.ACTIVE, pageable);
        } catch (DataAccessException dbEx) {
            log.warn("DB unavailable for search query, attempting Redis stale read", dbEx);
            // Return degraded response (empty) if both DB and cache are unavailable
            return new SearchResponse(searchSessionId, true, List.of(), query.page(), pageSize, 0, 0);
        }

        long days = computeDays(query);
        List<FacilityResult> results = facilityPage.getContent().stream()
                .filter(f -> isWithinProximity(f, query))
                .filter(f -> matchesFeatureFilter(f, query.features()))
                .map(facility -> toResult(facility, window, days))
                .collect(Collectors.toList());

        return new SearchResponse(
                searchSessionId,
                stale,
                results,
                facilityPage.getNumber(),
                facilityPage.getSize(),
                facilityPage.getTotalPages(),
                facilityPage.getTotalElements());
    }

    private long computeDays(SearchParkingQuery query) {
        if (query.checkIn() == null || query.checkOut() == null) return 1L;
        long days = ChronoUnit.DAYS.between(query.checkIn(), query.checkOut());
        return Math.max(1L, days);
    }

    private boolean isWithinProximity(ParkingFacility facility, SearchParkingQuery query) {
        if (query.lat() == null || query.lng() == null || query.radiusKm() == null) {
            return true; // no proximity filter applied
        }
        double facilityLat = facility.getLocation().latitude();
        double facilityLng = facility.getLocation().longitude();
        double distKm = haversineKm(query.lat(), query.lng(), facilityLat, facilityLng);
        return distKm <= query.radiusKm();
    }

    private boolean matchesFeatureFilter(ParkingFacility facility, List<String> features) {
        if (features == null || features.isEmpty()) return true;
        Set<String> facilityTags = facility.getTags().stream()
                .map(FacilityTag::name)
                .collect(Collectors.toSet());
        return features.stream().allMatch(facilityTags::contains);
    }

    private FacilityResult toResult(ParkingFacility facility, AvailabilityWindow window, long days) {
        int availableSpots;
        boolean staleData = false;
        try {
            availableSpots = availabilityCache
                    .getAvailableSpotCount(facility.getId(), window)
                    .orElseGet(() -> {
                        int count = facility.getTotalSpots();
                        availabilityCache.putAvailableSpotCount(facility.getId(), window, count);
                        return count;
                    });
        } catch (Exception cacheEx) {
            log.warn("Redis unavailable for facility {}, using default spot count", facility.getId(), cacheEx);
            availableSpots = facility.getTotalSpots();
            staleData = true;
        }

        boolean lowAvailability = availableSpots < lowAvailabilityThreshold;

        BigDecimal dailyRate = facility.getDailyRate() != null ? facility.getDailyRate() : BigDecimal.ZERO;
        String currency = facility.getCurrency() != null ? facility.getCurrency() : "EUR";
        BigDecimal estimatedAmount = dailyRate.multiply(BigDecimal.valueOf(days));
        Money estimatedPrice = Money.of(estimatedAmount, currency);

        return new FacilityResult(
                facility.getId().toString(),
                facility.getName(),
                facility.getCity(),
                facility.getAddress(),
                facility.getLocation().latitude(),
                facility.getLocation().longitude(),
                dailyRate,
                currency,
                facility.getTags().stream()
                        .map(FacilityTag::name)
                        .collect(Collectors.toSet()),
                lowAvailability,
                availableSpots,
                estimatedPrice);
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
