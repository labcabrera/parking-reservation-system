package org.labcabrera.parking.facilities.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.facilities.application.port.DynamicPrice;
import org.labcabrera.parking.facilities.application.port.DynamicPricingRequest;
import org.labcabrera.parking.facilities.application.port.DynamicPricingSlotType;
import org.labcabrera.parking.facilities.application.port.SlotOccupancy;
import org.labcabrera.parking.facilities.generated.client.pricing.api.PricingApi;
import org.labcabrera.parking.facilities.generated.client.pricing.api.PricingRulesApi;
import org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicPriceBreakdown;
import org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateResponse;
import org.labcabrera.parking.facilities.generated.client.pricing.model.PricingRule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

class PricingServiceClientAdapterTest {

    private final PricingRulesApi pricingRulesApi = mock(PricingRulesApi.class);
    private final PricingApi pricingApi = mock(PricingApi.class);
    private final PricingServiceClientAdapter adapter = new PricingServiceClientAdapter(pricingRulesApi, pricingApi);

    @Test
    void existsByIdReturnsTrueWhenPricingRuleIsFound() {
        UUID pricingRuleId = UUID.randomUUID();
        when(pricingRulesApi.getPricingRuleById(pricingRuleId)).thenReturn(new PricingRule().id(pricingRuleId));

        assertThat(adapter.existsById(pricingRuleId)).isTrue();
    }

    @Test
    void existsByIdReturnsFalseWhenPricingRuleIsNotFound() {
        UUID pricingRuleId = UUID.randomUUID();
        when(pricingRulesApi.getPricingRuleById(pricingRuleId)).thenThrow(notFound());

        assertThat(adapter.existsById(pricingRuleId)).isFalse();
    }

    @Test
    void calculateDynamicPriceMapsRequestAndResponse() {
        UUID pricingRuleId = UUID.randomUUID();
        when(pricingApi.calculateDynamicRate(any())).thenReturn(new DynamicRateResponse()
            .pricingRuleId(pricingRuleId)
            .slotType(DynamicRateResponse.SlotTypeEnum.SHORT)
            .slotCount(2)
            .averageOccupancyRate(new BigDecimal("0.7500"))
            .averageOccupancyMultiplier(new BigDecimal("1.7500"))
            .price(new DynamicPriceBreakdown()
                .baseAmount(new BigDecimal("35.0000"))
                .taxAmount(new BigDecimal("7.3500"))
                .totalAmount(new BigDecimal("42.3500"))
                .currency("EUR")));

        DynamicPrice price = adapter.calculateDynamicPrice(new DynamicPricingRequest(
            pricingRuleId,
            DynamicPricingSlotType.SHORT,
            List.of(new SlotOccupancy(new BigDecimal("0.5000")), new SlotOccupancy(new BigDecimal("1.0000")))));

        assertThat(price.pricingRuleId()).isEqualTo(pricingRuleId);
        assertThat(price.slotType()).isEqualTo(DynamicPricingSlotType.SHORT);
        assertThat(price.slotCount()).isEqualTo(2);
        assertThat(price.averageOccupancyRate()).isEqualByComparingTo(new BigDecimal("0.7500"));
        assertThat(price.averageOccupancyMultiplier()).isEqualByComparingTo(new BigDecimal("1.7500"));
        assertThat(price.baseAmount()).isEqualByComparingTo(new BigDecimal("35.0000"));
        assertThat(price.taxAmount()).isEqualByComparingTo(new BigDecimal("7.3500"));
        assertThat(price.totalAmount()).isEqualByComparingTo(new BigDecimal("42.3500"));
        assertThat(price.currency()).isEqualTo("EUR");

        verify(pricingApi).calculateDynamicRate(org.mockito.ArgumentMatchers.argThat(request ->
            request.getPricingRuleId().equals(pricingRuleId)
                && request.getSlotType() == org.labcabrera.parking.facilities.generated.client.pricing.model.DynamicRateRequest.SlotTypeEnum.SHORT
                && request.getSlots().size() == 2
                && request.getSlots().get(0).getOccupancyRate().compareTo(new BigDecimal("0.5000")) == 0));
    }

    private RestClientResponseException notFound() {
        return new RestClientResponseException(
            "Not found",
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            HttpHeaders.EMPTY,
            new byte[0],
            StandardCharsets.UTF_8);
    }
}
