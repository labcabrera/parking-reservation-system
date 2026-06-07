package org.labcabrera.parking.facilities.application.port;

import java.util.UUID;

public interface PricingServicePort {

    boolean existsById(UUID pricingRuleId);

    DynamicPrice calculateDynamicPrice(DynamicPricingRequest request);
}
