package org.labcabrera.parking.catalog.domain.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.valueobjects.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.valueobjects.Coordinates;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityTag;
import org.labcabrera.parking.catalog.domain.valueobjects.ParkingPricingRule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParkingFacility {

    private final FacilityId id;
    private String name;
    private String city;
    private String address;
    private Coordinates location;
    private int totalSpots;
    private final Set<FacilityTag> tags;
    private FacilityStatus status;
    private CancellationPolicy cancellationPolicy;
    private ParkingPricingRule pricingRule;
    private Long version;

    public ParkingFacility(
        String name,
        String city,
        String address,
        Coordinates location,
        int totalSpots,
        final Set<FacilityTag> tags,
        FacilityStatus status,
        CancellationPolicy cancellationPolicy,
        ParkingPricingRule pricingRule) {

        if (totalSpots < 1) {
            throw new IllegalArgumentException("totalSpots must be at least 1");
        }
        this.id = new FacilityId(UUID.randomUUID());
        this.name = name;
        this.city = city;
        this.address = address;
        this.location = location;
        this.totalSpots = totalSpots;
        this.tags = tags != null ? EnumSet.copyOf(tags) : EnumSet.noneOf(FacilityTag.class);
        this.status = status;
        this.cancellationPolicy = cancellationPolicy;
        this.version = 0L;
    }

    public boolean isSearchable() {
        return status == FacilityStatus.ACTIVE;
    }

}
