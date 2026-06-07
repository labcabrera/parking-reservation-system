package org.labcabrera.parking.pricing.interfaces.rest.mapper;

import org.labcabrera.parking.pricing.application.cqrs.query.CalculateDynamicRateQuery;
import org.labcabrera.parking.pricing.domain.valueobject.DynamicPricingResult;
import org.labcabrera.parking.pricing.domain.valueobject.SlotOccupancy;
import org.labcabrera.parking.pricing.interfaces.rest.dto.DynamicRateRequest;
import org.labcabrera.parking.pricing.interfaces.rest.dto.DynamicRateResponse;
import org.labcabrera.parking.pricing.interfaces.rest.dto.DynamicPriceBreakdownDto;
import org.springframework.stereotype.Component;

@Component
public class DynamicPricingRestMapper {

    public CalculateDynamicRateQuery toQuery(DynamicRateRequest request) {
        return new CalculateDynamicRateQuery(
            request.pricingRuleId(),
            request.slotType(),
            request.slots().stream()
                .map(slot -> new SlotOccupancy(slot.occupancyRate()))
                .toList());
    }

    public DynamicRateResponse toResponse(DynamicPricingResult result) {
        return new DynamicRateResponse(
            result.pricingRuleId(),
            result.slotType(),
            result.slotCount(),
            result.averageOccupancyRate(),
            result.averageOccupancyMultiplier(),
            new DynamicPriceBreakdownDto(
                result.price().baseAmount(),
                result.price().taxAmount(),
                result.price().totalAmount(),
                result.price().currency()));
    }
}
