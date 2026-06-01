package org.labcabrera.parking.catalog.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "parking_spot", schema = "catalog")
public class ParkingSpotJpaEntity {

    @Id
    private UUID id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "spot_number", nullable = false, length = 20)
    private String spotNumber;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "availability_status", nullable = false, length = 20)
    private String availabilityStatus;

    @Version
    private Long version;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected ParkingSpotJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFacilityId() { return facilityId; }
    public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }

    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
