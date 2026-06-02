package org.labcabrera.parking.catalog.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "parking_facility", schema = "catalog")
@Data
public class ParkingFacilityJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "total_spots", nullable = false)
    private int totalSpots;

    @Column(columnDefinition = "text[]")
    private String[] tags;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "free_cancel_hours", nullable = false)
    private int freeCancelHours;

    @Column(name = "penalty_cancel_minutes", nullable = false)
    private int penaltyCancelMinutes;

    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    public void updateFrom(org.labcabrera.parking.catalog.domain.model.ParkingFacility domain) {
        if (domain == null) {
            return;
        }
        if (domain.getId() != null) {
            this.id = domain.getId().value();
        }
        this.name = domain.getName();
        this.city = domain.getCity();
        this.address = domain.getAddress();
        if (domain.getLocation() != null) {
            this.latitude = domain.getLocation().latitude();
            this.longitude = domain.getLocation().longitude();
        }
        this.totalSpots = domain.getTotalSpots();
        if (domain.getTags() != null) {
            this.tags = domain.getTags().stream().map(Enum::name).toArray(String[]::new);
        }
        else {
            this.tags = null;
        }
        this.status = domain.getStatus() != null ? domain.getStatus().name() : null;
        if (domain.getCancellationPolicy() != null) {
            this.freeCancelHours = domain.getCancellationPolicy().freeCancelHours();
            this.penaltyCancelMinutes = domain.getCancellationPolicy().penaltyCancelMinutes();
        }
        if (domain.getPricingRule() != null) {
            this.dailyRate = domain.getPricingRule().estimatedDailyPrice();
            if (this.currency == null) {
                this.currency = "EUR";
            }
        }
        this.version = domain.getVersion();
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        this.updatedAt = OffsetDateTime.now();
    }

}
