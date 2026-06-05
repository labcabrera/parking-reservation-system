package org.labcabrera.parking.ecommerce.infrastructure.jpa.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "payment_methods",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_methods_code", columnNames = "code")
    })
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethodType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethodStatus status;

    @Column(name = "gateway_provider", nullable = false, length = 60)
    private String gatewayProvider;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
