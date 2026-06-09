package org.labcabrera.parking.facilities.infrastructure.config;

import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.labcabrera.parking.facilities.domain.valueobject.ReservationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FacilitiesAxonConfig {

    public FacilitiesAxonConfig(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerSubscribingEventProcessor("reservation-projection");
    }

    @Bean
    public ReservationConfig reservationConfig(@Value("${catalog.reservation.hold-minutes:10}") int holdMinutes) {
        return new ReservationConfig(holdMinutes);
    }

    @Bean
    public DeadlineManager deadlineManager(org.axonframework.config.Configuration axonConfiguration,
        PlatformTransactionManager transactionManager) {
        return SimpleDeadlineManager.builder()
            .scopeAwareProvider(new ConfigurationScopeAwareProvider(axonConfiguration))
            .transactionManager(new SpringTransactionManager(transactionManager))
            .build();
    }
}
