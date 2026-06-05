package org.labcabrera.parking.ecommerce.infrastructure.config;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.GenericJpaRepository;
import org.axonframework.modelling.command.Repository;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderAxonConfig {

    @Bean(name = "orderRepository")
    public Repository<Order> orderRepository(
            EntityManagerProvider entityManagerProvider,
            EventBus eventBus,
            @Nonnull ParameterResolverFactory parameterResolverFactory) {
        return GenericJpaRepository.builder(Order.class)
            .entityManagerProvider(entityManagerProvider)
            .identifierConverter(UUID::fromString)
            .eventBus(eventBus)
            .parameterResolverFactory(parameterResolverFactory)
            .build();
    }
}
