package org.labcabrera.parking.catalog.domain.valueobjects;

public class ParkingSpot {

    private final SpotId id;
    private final FacilityId facilityId;
    private String spotNumber;
    private SpotType type;
    private SpotAvailabilityStatus availabilityStatus;
    private Long version;

    public ParkingSpot(
        SpotId id,
        FacilityId facilityId,
        String spotNumber,
        SpotType type,
        SpotAvailabilityStatus availabilityStatus,
        Long version) {
        this.id = id;
        this.facilityId = facilityId;
        this.spotNumber = spotNumber;
        this.type = type;
        this.availabilityStatus = availabilityStatus;
        this.version = version;
    }

    public boolean isAvailable() {
        return availabilityStatus == SpotAvailabilityStatus.AVAILABLE;
    }

    public SpotId getId() {
        return id;
    }

    public FacilityId getFacilityId() {
        return facilityId;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public SpotType getType() {
        return type;
    }

    public SpotAvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public Long getVersion() {
        return version;
    }
}
