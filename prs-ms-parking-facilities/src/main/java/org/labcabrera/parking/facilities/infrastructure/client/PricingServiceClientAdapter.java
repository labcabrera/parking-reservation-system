package org.labcabrera.parking.facilities.infrastructure.client;

import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.facilities.application.port.DynamicPrice;
import org.labcabrera.parking.facilities.application.port.DynamicPricingRequest;
import org.labcabrera.parking.facilities.application.port.DynamicPricingSlotType;
import org.labcabrera.parking.facilities.application.port.PricingServicePort;
import org.labcabrera.parking.facilities.generated.client.pricing.api.PricingApi;
import org.labcabrera.parking.facilities.generated.client.pricing.api.PricingRulesApi;
import org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicPriceBreakdown;
import org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PricingServiceClientAdapter implements PricingServicePort {

    private final PricingRulesApi pricingRulesApi;
    private final PricingApi pricingApi;

    @Override
    public boolean existsById(UUID pricingRuleId) {
        try {
            return pricingRulesApi.getPricingRuleById(pricingRuleId) != null;
        }
        catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public DynamicPrice calculateDynamicPrice(DynamicPricingRequest request) {
        DynamicRateResponse response = pricingApi.calculateDynamicRate(toGeneratedRequest(request));
        return toPortDynamicPrice(response);
    }

    private org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateRequest toGeneratedRequest(
        DynamicPricingRequest request) {

        return new org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateRequest()
            .pricingRuleId(request.pricingRuleId())
            .slotType(toGeneratedSlotType(request.slotType()))
            .slots(toGeneratedSlots(request.slots()));
    }

    private List<org.labcabrera.parking.facilities.generated.client.pricing.model.SlotOccupancy> toGeneratedSlots(
        List<org.labcabrera.parking.facilities.application.port.SlotOccupancy> slots) {

        return slots.stream()
            .map(slot -> new org.labcabrera.parking.facilities.generated.client.pricing.model.SlotOccupancy()
                .occupancyRate(slot.occupancyRate()))
            .toList();
    }

    private org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateRequest.SlotTypeEnum toGeneratedSlotType(
        DynamicPricingSlotType slotType) {

        return switch (slotType) {
            case SHORT -> org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateRequest.SlotTypeEnum.SHORT;
            case LONG -> org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateRequest.SlotTypeEnum.LONG;
        };
    }

    private DynamicPrice toPortDynamicPrice(DynamicRateResponse response) {
        DynamicPriceBreakdown price = response.getPrice();
        return new DynamicPrice(
            response.getPricingRuleId(),
            toPortSlotType(response.getSlotType()),
            response.getSlotCount(),
            response.getAverageOccupancyRate(),
            response.getAverageOccupancyMultiplier(),
            price.getBaseAmount(),
            price.getTaxAmount(),
            price.getTotalAmount(),
            price.getCurrency());
    }

    private DynamicPricingSlotType toPortSlotType(DynamicRateResponse.SlotTypeEnum slotType) {
        return switch (slotType) {
            case SHORT -> DynamicPricingSlotType.SHORT;
            case LONG -> DynamicPricingSlotType.LONG;
        };
    }
}
