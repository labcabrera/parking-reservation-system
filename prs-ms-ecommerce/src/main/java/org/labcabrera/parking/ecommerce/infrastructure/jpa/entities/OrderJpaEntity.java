package org.labcabrera.parking.ecommerce.infrastructure.jpa.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "hold_id", nullable = false, unique = true)
    private UUID holdId;

    @Column(name = "booking_session_id", length = 100)
    private String bookingSessionId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Embedded
    private MoneyEmbeddable money;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_payment_attempt_id")
    private UUID lastPaymentAttemptId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Version
    private Long version;
}
