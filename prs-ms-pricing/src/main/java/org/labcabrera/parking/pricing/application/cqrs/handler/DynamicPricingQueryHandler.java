package org.labcabrera.parking.pricing.application.cqrs.handler;

import org.labcabrera.parking.pricing.application.cqrs.query.CalculateDynamicRateQuery;
import org.labcabrera.parking.pricing.application.port.PricingRuleRepository;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.pricing.domain.service.PricingCalculationService;
import org.labcabrera.parking.pricing.domain.valueobject.DynamicPricingResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DynamicPricingQueryHandler {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingCalculationService pricingCalculationService;

    @Transactional(readOnly = true)
    public DynamicPricingResult handle(CalculateDynamicRateQuery query) {
        PricingRule pricingRule = pricingRuleRepository.findById(query.pricingRuleId())
            .orElseThrow(() -> new EntityNotFoundException("Pricing rule not found: " + query.pricingRuleId()));
        return pricingCalculationService.calculateDynamicRate(pricingRule, query.slotType(), query.slots());
    }
}
