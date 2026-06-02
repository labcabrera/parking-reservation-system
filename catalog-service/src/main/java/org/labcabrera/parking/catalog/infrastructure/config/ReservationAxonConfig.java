package org.labcabrera.parking.catalog.infrastructure.config;

import java.util.UUID;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.GenericJpaRepository;
import org.axonframework.modelling.command.Repository;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.labcabrera.parking.catalog.domain.aggregate.ReservationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the state-stored Axon aggregate {@link Reservation} to JPA and exposes
 * runtime configuration (hold TTL) consumed by the aggregate constructor.
 */
@Configuration
public class ReservationAxonConfig {

    @Bean(name = "reservationRepository")
    public Repository<Reservation> reservationRepository(
            EntityManagerProvider entityManagerProvider,
            EventBus eventBus,
            ParameterResolverFactory parameterResolverFactory) {
        return GenericJpaRepository.builder(Reservation.class)
            .entityManagerProvider(entityManagerProvider)
            .identifierConverter(UUID::fromString)
            .eventBus(eventBus)
            .parameterResolverFactory(parameterResolverFactory)
            .build();
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
