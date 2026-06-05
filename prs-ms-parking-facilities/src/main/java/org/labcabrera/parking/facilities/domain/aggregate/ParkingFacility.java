package org.labcabrera.parking.facilities.domain.aggregate;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.facilities.domain.valueobject.Coordinates;
import org.labcabrera.parking.facilities.domain.valueobject.EntityMetadata;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityTag;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingPricingRule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ParkingFacility {

    private final FacilityId id;
    private String name;
    private String city;
    private String address;
    private Coordinates location;
    private ParkingCapacity capacity;
    private Set<FacilityTag> tags;
    private FacilityStatus status;
    private CancellationPolicy cancellationPolicy;
    private ParkingPricingRule pricingRule;
    private EntityMetadata metadata;
    private Long version;

    public ParkingFacility(
        String name,
        String city,
        String address,
        Coordinates location,
        ParkingCapacity capacity,
        Set<FacilityTag> tags,
        FacilityStatus status,
        CancellationPolicy cancellationPolicy,
        ParkingPricingRule pricingRule) {

        if (capacity == null) {
            throw new IllegalArgumentException("capacity is required");
        }
        this.id = new FacilityId(UUID.randomUUID());
        this.name = name;
        this.city = city;
        this.address = address;
        this.location = location;
        this.capacity = capacity;
        this.tags = tags != null ? EnumSet.copyOf(tags) : EnumSet.noneOf(FacilityTag.class);
        this.status = status;
        this.cancellationPolicy = cancellationPolicy;
        this.pricingRule = pricingRule;
        this.metadata = new EntityMetadata(LocalDateTime.now(), Optional.empty(), "system");
        this.version = null;
    }

    @JsonCreator
    public ParkingFacility(
        @JsonProperty("id") FacilityId id,
        @JsonProperty("name") String name,
        @JsonProperty("city") String city,
        @JsonProperty("address") String address,
        @JsonProperty("location") Coordinates location,
        @JsonProperty("capacity") ParkingCapacity capacity,
        @JsonProperty("tags") Set<FacilityTag> tags,
        @JsonProperty("status") FacilityStatus status,
        @JsonProperty("cancellationPolicy") CancellationPolicy cancellationPolicy,
        @JsonProperty("pricingRule") ParkingPricingRule pricingRule,
        @JsonProperty("metadata") EntityMetadata metadata,
        @JsonProperty("version") Long version) {

        this.id = id != null ? id : new FacilityId(UUID.randomUUID());
        this.name = name;
        this.city = city;
        this.address = address;
        this.location = location;
        this.capacity = capacity;
        this.tags = tags != null ? EnumSet.copyOf(tags) : EnumSet.noneOf(FacilityTag.class);
        this.status = status;
        this.cancellationPolicy = cancellationPolicy;
        this.pricingRule = pricingRule;
        this.metadata = metadata != null ? metadata : new EntityMetadata(LocalDateTime.now(), Optional.empty(), "system");
        this.version = version;
    }

    public boolean isSearchable() {
        return status == FacilityStatus.ACTIVE;
    }

    public int getTotalSpots() {
        return capacity.total();
    }

}
