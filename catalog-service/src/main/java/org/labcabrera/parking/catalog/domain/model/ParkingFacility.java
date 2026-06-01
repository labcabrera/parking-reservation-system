package org.labcabrera.parking.catalog.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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
    private BigDecimal dailyRate;
    private String currency;
    private Long version;

    public ParkingFacility(
            FacilityId id,
            String name,
            String city,
            String address,
            Coordinates location,
            int totalSpots,
            Set<FacilityTag> tags,
            FacilityStatus status,
            CancellationPolicy cancellationPolicy,
            Long version) {
        this(id, name, city, address, location, totalSpots, tags, status, cancellationPolicy, BigDecimal.ZERO, "EUR", version);
    }

    public ParkingFacility(
            FacilityId id,
            String name,
            String city,
            String address,
            Coordinates location,
            int totalSpots,
            Set<FacilityTag> tags,
            FacilityStatus status,
            CancellationPolicy cancellationPolicy,
            BigDecimal dailyRate,
            String currency,
            Long version) {
        if (totalSpots < 1) {
            throw new IllegalArgumentException("totalSpots must be at least 1");
        }
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.location = location;
        this.totalSpots = totalSpots;
        this.tags = tags != null ? EnumSet.copyOf(tags) : EnumSet.noneOf(FacilityTag.class);
        this.status = status;
        this.cancellationPolicy = cancellationPolicy;
        this.dailyRate = dailyRate != null ? dailyRate : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "EUR";
        this.version = version;
    }

    public boolean isSearchable() {
        return status == FacilityStatus.ACTIVE;
    }

    public FacilityId getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public Coordinates getLocation() { return location; }
    public int getTotalSpots() { return totalSpots; }
    public Set<FacilityTag> getTags() { return Collections.unmodifiableSet(tags); }
    public FacilityStatus getStatus() { return status; }
    public CancellationPolicy getCancellationPolicy() { return cancellationPolicy; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public String getCurrency() { return currency; }
    public Long getVersion() { return version; }
}
