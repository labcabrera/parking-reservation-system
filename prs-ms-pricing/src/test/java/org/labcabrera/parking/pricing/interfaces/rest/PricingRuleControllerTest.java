package org.labcabrera.parking.pricing.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.pricing.application.service.PricingRuleService;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;
import org.labcabrera.parking.pricing.interfaces.rest.dto.PageResponse;
import org.labcabrera.parking.pricing.interfaces.rest.dto.PricingRuleDto;
import org.labcabrera.parking.pricing.interfaces.rest.mapper.PricingRuleRestMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

class PricingRuleControllerTest {

    private final PricingRuleService service = mock(PricingRuleService.class);
    private final PricingRuleController controller = new PricingRuleController(service, new PricingRuleRestMapper());

    @Test
    void listReturnsCustomPageResponse() {
        var pageable = PageRequest.of(1, 2);
        var rule = pricingRule(UUID.randomUUID(), "Standard daily");
        when(service.list(pageable)).thenReturn(new PageImpl<>(List.of(rule), pageable, 5));

        ResponseEntity<PageResponse<PricingRuleDto>> response = controller.list(pageable);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).name()).isEqualTo("Standard daily");
        assertThat(response.getBody().pagination().page()).isEqualTo(1);
        assertThat(response.getBody().pagination().size()).isEqualTo(2);
        assertThat(response.getBody().pagination().totalElements()).isEqualTo(5);
        assertThat(response.getBody().pagination().totalPages()).isEqualTo(3);
    }

    private PricingRule pricingRule(UUID id, String name) {
        return new PricingRule(
            id,
            name,
            BillingType.DAILY,
            new Money(new BigDecimal("1.00"), "EUR"),
            null,
            new Money(new BigDecimal("12.00"), "EUR"),
            BigDecimal.ZERO,
            PricingRuleStatus.ACTIVE,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            null,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            null,
            1L);
    }
}
