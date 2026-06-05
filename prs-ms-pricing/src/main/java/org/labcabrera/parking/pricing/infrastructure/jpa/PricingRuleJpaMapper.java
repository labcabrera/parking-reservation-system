package org.labcabrera.parking.pricing.infrastructure.jpa;

import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.infrastructure.jpa.entities.PricingRuleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PricingRuleJpaMapper {

    public PricingRule toDomain(PricingRuleJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PricingRule(
            entity.getId(),
            entity.getFacilityId(),
            entity.getName(),
            entity.getBillingType(),
            new Money(entity.getBaseAmount(), entity.getBaseCurrency()),
            moneyOrNull(entity.getHourlyAmount(), entity.getHourlyCurrency()),
            moneyOrNull(entity.getDailyAmount(), entity.getDailyCurrency()),
            entity.getTaxRate(),
            entity.getStatus(),
            entity.getValidFrom(),
            entity.getValidTo(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion());
    }

    public PricingRuleJpaEntity toEntity(PricingRule domain) {
        PricingRuleJpaEntity entity = new PricingRuleJpaEntity();
        entity.setId(domain.getId());
        entity.setFacilityId(domain.getFacilityId());
        entity.setName(domain.getName());
        entity.setBillingType(domain.getBillingType());
        entity.setBaseAmount(domain.getBaseRate().amount());
        entity.setBaseCurrency(domain.getBaseRate().currency());
        if (domain.getHourlyRate() != null) {
            entity.setHourlyAmount(domain.getHourlyRate().amount());
            entity.setHourlyCurrency(domain.getHourlyRate().currency());
        }
        if (domain.getDailyRate() != null) {
            entity.setDailyAmount(domain.getDailyRate().amount());
            entity.setDailyCurrency(domain.getDailyRate().currency());
        }
        entity.setTaxRate(domain.getTaxRate());
        entity.setStatus(domain.getStatus());
        entity.setValidFrom(domain.getValidFrom());
        entity.setValidTo(domain.getValidTo());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    private Money moneyOrNull(java.math.BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return new Money(amount, currency);
    }
}
