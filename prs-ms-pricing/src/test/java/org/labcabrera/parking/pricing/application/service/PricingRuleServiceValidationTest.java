package org.labcabrera.parking.pricing.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.pricing.application.port.PricingRuleRepository;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.valueobject.BillingType;
import org.labcabrera.parking.pricing.domain.valueobject.Money;
import org.labcabrera.parking.pricing.domain.valueobject.PricingRuleStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.ConstraintViolationException;

@SpringJUnitConfig(classes = PricingRuleServiceValidationTest.Config.class)
class PricingRuleServiceValidationTest {

    @jakarta.annotation.Resource
    private PricingRuleService service;

    @Test
    void rejectsTaxRateGreaterThanOne() {
        assertThatThrownBy(() -> service.create(pricingRule(new BigDecimal("21.00"))))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("taxRate");
    }

    @Test
    void rejectsTaxRateWithMoreThanFourDecimals() {
        assertThatThrownBy(() -> service.create(pricingRule(new BigDecimal("0.12345"))))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("taxRate");
    }

    private PricingRule pricingRule(BigDecimal taxRate) {
        return new PricingRule(
            "Airport daily rate",
            BillingType.DAILY,
            new Money(new BigDecimal("5.00"), "EUR"),
            null,
            new Money(new BigDecimal("30.00"), "EUR"),
            taxRate,
            PricingRuleStatus.ACTIVE,
            LocalDateTime.now(),
            null);
    }

    @Configuration
    static class Config {

        @Bean
        MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }

        @Bean
        PricingRuleRepository pricingRuleRepository() {
            return mock(PricingRuleRepository.class);
        }

        @Bean
        PricingRuleService pricingRuleService(PricingRuleRepository repository) {
            return new PricingRuleService(repository);
        }
    }
}
