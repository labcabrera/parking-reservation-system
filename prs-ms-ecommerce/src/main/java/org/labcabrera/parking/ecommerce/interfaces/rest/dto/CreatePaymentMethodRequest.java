package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(name = "CreatePaymentMethodRequest", description = "Request payload to register a supported payment method")
public record CreatePaymentMethodRequest(

    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_]+")
    String code,

    @NotBlank
    String displayName,

    @NotNull
    PaymentMethodType type,

    @NotBlank
    String gatewayProvider,

    String iconUrl,

    @Min(0)
    int displayOrder) {
}
