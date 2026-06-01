package org.labcabrera.parking.reservation.infrastructure.persistence;

import jakarta.persistence.*;
import org.labcabrera.parking.reservation.domain.model.HoldStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "spot_hold", schema = "reservation")
public class SpotHoldJpaEntity {

    @Id
    private UUID id;

    @Column(name = "search_session_id", nullable = false, length = 36)
    private String searchSessionId;

    @Column(name = "spot_id", nullable = false)
    private UUID spotId;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "visitor_ip", length = 45)
    private String visitorIp;

    @Column(name = "check_in", nullable = false)
    private Instant checkIn;

    @Column(name = "check_out", nullable = false)
    private Instant checkOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HoldStatus status;

    @Column(name = "estimated_price_amount", precision = 10, scale = 2)
    private BigDecimal estimatedPriceAmount;

    @Column(name = "estimated_price_currency", length = 3)
    private String estimatedPriceCurrency;

    @Column(name = "confirmed_price_amount", precision = 10, scale = 2)
    private BigDecimal confirmedPriceAmount;

    @Column(name = "confirmed_price_currency", length = 3)
    private String confirmedPriceCurrency;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected SpotHoldJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSearchSessionId() { return searchSessionId; }
    public void setSearchSessionId(String searchSessionId) { this.searchSessionId = searchSessionId; }

    public UUID getSpotId() { return spotId; }
    public void setSpotId(UUID spotId) { this.spotId = spotId; }

    public UUID getFacilityId() { return facilityId; }
    public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }

    public String getVisitorIp() { return visitorIp; }
    public void setVisitorIp(String visitorIp) { this.visitorIp = visitorIp; }

    public Instant getCheckIn() { return checkIn; }
    public void setCheckIn(Instant checkIn) { this.checkIn = checkIn; }

    public Instant getCheckOut() { return checkOut; }
    public void setCheckOut(Instant checkOut) { this.checkOut = checkOut; }

    public HoldStatus getStatus() { return status; }
    public void setStatus(HoldStatus status) { this.status = status; }

    public BigDecimal getEstimatedPriceAmount() { return estimatedPriceAmount; }
    public void setEstimatedPriceAmount(BigDecimal estimatedPriceAmount) { this.estimatedPriceAmount = estimatedPriceAmount; }

    public String getEstimatedPriceCurrency() { return estimatedPriceCurrency; }
    public void setEstimatedPriceCurrency(String estimatedPriceCurrency) { this.estimatedPriceCurrency = estimatedPriceCurrency; }

    public BigDecimal getConfirmedPriceAmount() { return confirmedPriceAmount; }
    public void setConfirmedPriceAmount(BigDecimal confirmedPriceAmount) { this.confirmedPriceAmount = confirmedPriceAmount; }

    public String getConfirmedPriceCurrency() { return confirmedPriceCurrency; }
    public void setConfirmedPriceCurrency(String confirmedPriceCurrency) { this.confirmedPriceCurrency = confirmedPriceCurrency; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
