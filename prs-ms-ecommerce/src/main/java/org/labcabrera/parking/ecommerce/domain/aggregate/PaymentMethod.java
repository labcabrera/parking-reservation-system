package org.labcabrera.parking.ecommerce.domain.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;

import lombok.Getter;

@Getter
public class PaymentMethod {

    private UUID id;
    private PaymentMethodCode code;
    private String displayName;
    private PaymentMethodType type;
    private PaymentMethodStatus status;
    private String gatewayProvider;
    private String iconUrl;
    private int displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public PaymentMethod(
        UUID id,
        PaymentMethodCode code,
        String displayName,
        PaymentMethodType type,
        PaymentMethodStatus status,
        String gatewayProvider,
        String iconUrl,
        int displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version) {
        this.id = requireId(id);
        this.code = requireCode(code);
        this.displayName = requireDisplayName(displayName);
        this.type = requireType(type);
        this.status = status == null ? PaymentMethodStatus.ACTIVE : status;
        this.gatewayProvider = requireGatewayProvider(gatewayProvider);
        this.iconUrl = normalizeOptional(iconUrl);
        this.displayOrder = Math.max(displayOrder, 0);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static PaymentMethod create(
        PaymentMethodCode code,
        String displayName,
        PaymentMethodType type,
        String gatewayProvider,
        String iconUrl,
        int displayOrder) {
        return new PaymentMethod(
            UUID.randomUUID(),
            code,
            displayName,
            type,
            PaymentMethodStatus.ACTIVE,
            gatewayProvider,
            iconUrl,
            displayOrder,
            LocalDateTime.now(),
            null,
            null);
    }

    public void update(
        String displayName,
        PaymentMethodType type,
        PaymentMethodStatus status,
        String gatewayProvider,
        String iconUrl,
        int displayOrder) {
        this.displayName = requireDisplayName(displayName);
        this.type = requireType(type);
        this.status = status == null ? PaymentMethodStatus.ACTIVE : status;
        this.gatewayProvider = requireGatewayProvider(gatewayProvider);
        this.iconUrl = normalizeOptional(iconUrl);
        this.displayOrder = Math.max(displayOrder, 0);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAvailableForCheckout() {
        return status.isActive();
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("payment method id is required");
        }
        return id;
    }

    private static PaymentMethodCode requireCode(PaymentMethodCode code) {
        if (code == null) {
            throw new IllegalArgumentException("payment method code is required");
        }
        return code;
    }

    private static PaymentMethodType requireType(PaymentMethodType type) {
        if (type == null) {
            throw new IllegalArgumentException("payment method type is required");
        }
        return type;
    }

    private static String requireDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("payment method display name is required");
        }
        return displayName.trim();
    }

    private static String requireGatewayProvider(String gatewayProvider) {
        if (gatewayProvider == null || gatewayProvider.isBlank()) {
            throw new IllegalArgumentException("payment method gateway provider is required");
        }
        return gatewayProvider.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
