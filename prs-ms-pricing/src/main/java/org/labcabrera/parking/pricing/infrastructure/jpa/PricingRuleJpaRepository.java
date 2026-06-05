package org.labcabrera.parking.pricing.infrastructure.jpa;

import java.util.UUID;

import org.labcabrera.parking.pricing.infrastructure.jpa.entities.PricingRuleJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRuleJpaRepository extends JpaRepository<PricingRuleJpaEntity, UUID> {

    Page<PricingRuleJpaEntity> findByFacilityId(UUID facilityId, Pageable pageable);
}
