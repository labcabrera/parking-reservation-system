package org.labcabrera.parking.pricing.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.pricing.application.port.PricingRuleRepository;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PricingRuleRepositoryAdapter implements PricingRuleRepository {

    private final PricingRuleJpaRepository repository;
    private final PricingRuleJpaMapper mapper;

    @Override
    public PricingRule save(PricingRule pricingRule) {
        return mapper.toDomain(repository.save(mapper.toEntity(pricingRule)));
    }

    @Override
    public Optional<PricingRule> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<PricingRule> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<PricingRule> findByFacilityId(UUID facilityId, Pageable pageable) {
        return repository.findByFacilityId(facilityId, pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
