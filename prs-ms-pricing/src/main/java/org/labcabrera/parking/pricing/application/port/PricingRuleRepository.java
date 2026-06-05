package org.labcabrera.parking.pricing.application.port;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PricingRuleRepository {

    PricingRule save(PricingRule pricingRule);

    Optional<PricingRule> findById(UUID id);

    Page<PricingRule> findAll(Pageable pageable);

    Page<PricingRule> findByFacilityId(UUID facilityId, Pageable pageable);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
