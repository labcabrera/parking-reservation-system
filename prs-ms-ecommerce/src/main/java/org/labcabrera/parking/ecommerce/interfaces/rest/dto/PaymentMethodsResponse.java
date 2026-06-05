package org.labcabrera.parking.ecommerce.interfaces.rest.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PaymentMethodsResponse", description = "Payment methods available at checkout")
public record PaymentMethodsResponse(

    List<PaymentMethodDto> methods) {
}
