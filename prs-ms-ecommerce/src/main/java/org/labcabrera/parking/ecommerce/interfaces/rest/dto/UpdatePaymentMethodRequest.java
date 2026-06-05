package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdatePaymentMethodRequest", description = "Request payload to update a supported payment method")
public record UpdatePaymentMethodRequest(

    @NotBlank
    String displayName,

    @NotNull
    PaymentMethodType type,

    @NotNull
    PaymentMethodStatus status,

    @NotBlank
    String gatewayProvider,

    String iconUrl,

    @Min(0)
    int displayOrder) {
}
