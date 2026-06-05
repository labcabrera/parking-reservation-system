package org.labcabrera.parking.ecommerce.interfaces.rest.mapper;

import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentMethod;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.CreatePaymentMethodRequest;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.PaymentMethodDto;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.UpdatePaymentMethodRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodMapper {

    public PaymentMethod toDomain(CreatePaymentMethodRequest request) {
        return PaymentMethod.create(
            new PaymentMethodCode(request.code()),
            request.displayName(),
            request.type(),
            request.gatewayProvider(),
            request.iconUrl(),
            request.displayOrder());
    }

    public PaymentMethod toDomain(PaymentMethod existing, UpdatePaymentMethodRequest request) {
        return new PaymentMethod(
            existing.getId(),
            existing.getCode(),
            request.displayName(),
            request.type(),
            request.status(),
            request.gatewayProvider(),
            request.iconUrl(),
            request.displayOrder(),
            existing.getCreatedAt(),
            existing.getUpdatedAt(),
            existing.getVersion());
    }

    public PaymentMethodDto toDto(PaymentMethod paymentMethod) {
        return new PaymentMethodDto(
            paymentMethod.getId(),
            paymentMethod.getCode().value(),
            paymentMethod.getDisplayName(),
            paymentMethod.getType(),
            paymentMethod.getStatus(),
            paymentMethod.getGatewayProvider(),
            paymentMethod.getIconUrl(),
            paymentMethod.getDisplayOrder(),
            paymentMethod.getCreatedAt(),
            paymentMethod.getUpdatedAt());
    }
}
