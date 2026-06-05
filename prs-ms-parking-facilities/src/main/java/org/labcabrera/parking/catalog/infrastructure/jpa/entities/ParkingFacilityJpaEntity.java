package org.labcabrera.parking.catalog.infrastructure.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityStatus;

@Entity
@Table(name = "parking_facility", schema = "public")
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

    @Column(name = "short_term_spots")
    private Integer shortTermSpots;

    @Column(name = "long_term_spots")
    private Integer longTermSpots;

    @Column(columnDefinition = "text[]")
    private String[] tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FacilityStatus status;

    @Column(name = "free_cancel_hours", nullable = false)
    private int freeCancelHours;

    @Column(name = "penalty_cancel_minutes", nullable = false)
    private int penaltyCancelMinutes;

    @Column(nullable = false, length = 20)
    private String externalPricingId;

    @Column(name = "estimated_daily_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedDailyPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void updateFrom(ParkingFacility domain) {
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
        this.totalSpots = domain.getCapacity().total();
        this.shortTermSpots = domain.getCapacity().shortTerm();
        this.longTermSpots = domain.getCapacity().longTerm();
        if (domain.getTags() != null) {
            this.tags = domain.getTags().stream().map(Enum::name).toArray(String[]::new);
        }
        else {
            this.tags = null;
        }
        this.status = domain.getStatus() != null ? domain.getStatus() : null;
        if (domain.getCancellationPolicy() != null) {
            this.freeCancelHours = domain.getCancellationPolicy().freeCancelHours();
            this.penaltyCancelMinutes = domain.getCancellationPolicy().penaltyCancelMinutes();
        }
        if (domain.getPricingRule() != null) {
            this.externalPricingId = domain.getPricingRule().externalPricingId();
            this.estimatedDailyPrice = domain.getPricingRule().estimatedDailyPrice();
        }
        if (domain.getMetadata() != null) {
            if (this.createdAt == null) {
                this.createdAt = domain.getMetadata().createdAt();
            }
            this.updatedAt = domain.getMetadata().updatedAt().orElse(LocalDateTime.now());
        }
        else {
            this.updatedAt = LocalDateTime.now();
        }
    }

}
