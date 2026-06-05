package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentMethod;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.labcabrera.parking.ecommerce.infrastructure.jpa.entities.PaymentMethodJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodJpaMapper {

    public PaymentMethod toDomain(PaymentMethodJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PaymentMethod(
            entity.getId(),
            new PaymentMethodCode(entity.getCode()),
            entity.getDisplayName(),
            entity.getType(),
            entity.getStatus(),
            entity.getGatewayProvider(),
            entity.getIconUrl(),
            entity.getDisplayOrder(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion());
    }

    public PaymentMethodJpaEntity toEntity(PaymentMethod paymentMethod) {
        PaymentMethodJpaEntity entity = new PaymentMethodJpaEntity();
        entity.setId(paymentMethod.getId());
        entity.setCode(paymentMethod.getCode().value());
        entity.setDisplayName(paymentMethod.getDisplayName());
        entity.setType(paymentMethod.getType());
        entity.setStatus(paymentMethod.getStatus());
        entity.setGatewayProvider(paymentMethod.getGatewayProvider());
        entity.setIconUrl(paymentMethod.getIconUrl());
        entity.setDisplayOrder(paymentMethod.getDisplayOrder());
        entity.setCreatedAt(paymentMethod.getCreatedAt());
        entity.setUpdatedAt(paymentMethod.getUpdatedAt());
        entity.setVersion(paymentMethod.getVersion());
        return entity;
    }
}
