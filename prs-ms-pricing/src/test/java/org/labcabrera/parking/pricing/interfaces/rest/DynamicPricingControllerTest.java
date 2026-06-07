package org.labcabrera.parking.pricing.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.pricing.application.cqrs.handler.DynamicPricingQueryHandler;
import org.labcabrera.parking.pricing.application.port.PricingRuleRepository;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.service.PricingCalculationService;
import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;
import org.labcabrera.parking.pricing.interfaces.rest.dto.DynamicRateRequest;
import org.labcabrera.parking.pricing.interfaces.rest.dto.SlotOccupancyRequest;
import org.labcabrera.parking.pricing.interfaces.rest.mapper.DynamicPricingRestMapper;

class DynamicPricingControllerTest {

    private final PricingRuleRepository repository = mock(PricingRuleRepository.class);
    private final DynamicPricingController controller = new DynamicPricingController(
        new DynamicPricingQueryHandler(repository, new PricingCalculationService()),
        new DynamicPricingRestMapper());

    @Test
    void calculateDynamicRateReturnsCalculatedRate() {
        UUID pricingRuleId = UUID.randomUUID();
        when(repository.findById(pricingRuleId)).thenReturn(Optional.of(pricingRule(pricingRuleId)));
        var request = new DynamicRateRequest(
            pricingRuleId,
            PricingSlotType.LONG,
            List.of(new SlotOccupancyRequest(new BigDecimal("0.2500")), new SlotOccupancyRequest(new BigDecimal("0.7500"))));

        var response = controller.calculateDynamicRate(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pricingRuleId()).isEqualTo(pricingRuleId);
        assertThat(response.getBody().slotType()).isEqualTo(PricingSlotType.LONG);
        assertThat(response.getBody().slotCount()).isEqualTo(2);
        assertThat(response.getBody().averageOccupancyRate()).isEqualByComparingTo(new BigDecimal("0.5000"));
        assertThat(response.getBody().averageOccupancyMultiplier()).isEqualByComparingTo(new BigDecimal("1.5000"));
        assertThat(response.getBody().price().baseAmount()).isEqualByComparingTo(new BigDecimal("36.0000"));
        assertThat(response.getBody().price().taxAmount()).isEqualByComparingTo(new BigDecimal("7.5600"));
        assertThat(response.getBody().price().totalAmount()).isEqualByComparingTo(new BigDecimal("43.5600"));
        assertThat(response.getBody().price().currency()).isEqualTo("EUR");
    }

    private PricingRule pricingRule(UUID id) {
        return new PricingRule(
            id,
            "Airport hybrid rate",
            BillingType.HYBRID,
            new Money(new BigDecimal("5.00"), "EUR"),
            new Money(new BigDecimal("20.00"), "EUR"),
            new Money(new BigDecimal("12.00"), "EUR"),
            new BigDecimal("0.2100"),
            PricingRuleStatus.ACTIVE,
            LocalDateTime.now(),
            null,
            LocalDateTime.now(),
            null,
            1L);
    }
}
