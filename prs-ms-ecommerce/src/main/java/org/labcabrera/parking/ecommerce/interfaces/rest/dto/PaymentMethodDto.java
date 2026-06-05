package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PaymentMethod", description = "Payment method supported by the ecommerce gateway")
public record PaymentMethodDto(

    UUID id,

    String code,

    String displayName,

    PaymentMethodType type,

    PaymentMethodStatus status,

    String gatewayProvider,

    String iconUrl,

    int displayOrder,

    LocalDateTime createdAt,

    LocalDateTime updatedAt) {
}
