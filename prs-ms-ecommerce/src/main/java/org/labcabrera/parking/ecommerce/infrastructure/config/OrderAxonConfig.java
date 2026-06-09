package org.labcabrera.parking.ecommerce.infrastructure.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderAxonConfig {

    public OrderAxonConfig(EventProcessingConfigurer eventProcessingConfigurer) {
        eventProcessingConfigurer.registerSubscribingEventProcessor("order-projection");
        // OrderSaga must use SubscribingEventProcessor so it receives events directly from the
        // event bus when eventGateway.publish() is called from the Kafka consumer thread.
        // TrackingEventProcessor (the default) reads from the event store asynchronously and
        // would never receive events published via eventGateway that are not domain events.
        // commandGateway.send() (non-blocking) is used inside the saga, so the Kafka thread
        // is not blocked.
        eventProcessingConfigurer.registerSubscribingEventProcessor("OrderSaga");
    }
}
