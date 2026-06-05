package org.labcabrera.parking.ecommerce.infrastructure.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderAxonConfig {

    public OrderAxonConfig(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerSubscribingEventProcessor("order-projection");
    }
}
