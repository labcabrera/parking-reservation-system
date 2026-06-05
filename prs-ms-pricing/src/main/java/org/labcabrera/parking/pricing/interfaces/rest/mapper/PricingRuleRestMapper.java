package org.labcabrera.parking.pricing.interfaces.rest.mapper;

import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.interfaces.rest.dto.CreatePricingRuleRequest;
import org.labcabrera.parking.pricing.interfaces.rest.dto.MoneyDto;
import org.labcabrera.parking.pricing.interfaces.rest.dto.PricingRuleDto;
import org.labcabrera.parking.pricing.interfaces.rest.dto.UpdatePricingRuleRequest;
import org.springframework.stereotype.Component;

@Component
public class PricingRuleRestMapper {

    public PricingRule toDomain(CreatePricingRuleRequest request) {
        return new PricingRule(
            request.name(),
            request.billingType(),
            toMoney(request.baseRate()),
            toMoney(request.hourlyRate()),
            toMoney(request.dailyRate()),
            request.taxRate(),
            request.status(),
            request.validFrom(),
            request.validTo());
    }

    public PricingRule toDomain(java.util.UUID id, UpdatePricingRuleRequest request) {
        return new PricingRule(
            id,
            request.name(),
            request.billingType(),
            toMoney(request.baseRate()),
            toMoney(request.hourlyRate()),
            toMoney(request.dailyRate()),
            request.taxRate(),
            request.status(),
            request.validFrom(),
            request.validTo(),
            null,
            null,
            null);
    }

    public PricingRuleDto toDto(PricingRule rule) {
        return new PricingRuleDto(
            rule.getId(),
            rule.getName(),
            rule.getBillingType(),
            toDto(rule.getBaseRate()),
            toDto(rule.getHourlyRate()),
            toDto(rule.getDailyRate()),
            rule.getTaxRate(),
            rule.getStatus(),
            rule.getValidFrom(),
            rule.getValidTo(),
            rule.getCreatedAt(),
            rule.getUpdatedAt(),
            rule.getVersion());
    }

    private Money toMoney(MoneyDto dto) {
        if (dto == null) {
            return null;
        }
        return new Money(dto.amount(), dto.currency());
    }

    private MoneyDto toDto(Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyDto(money.amount(), money.currency());
    }
}
