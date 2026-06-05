package org.labcabrera.parking.catalog.infrastructure.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.valueobject.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class ReservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "check_in", nullable = false)
    private LocalDateTime checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDateTime checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "estimated_price", precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Version
    private Long version;
}
