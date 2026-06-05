package org.labcabrera.parking.pricing.application.service;

import java.util.UUID;

import org.labcabrera.parking.pricing.application.port.PricingRuleRepository;
import org.labcabrera.parking.pricing.domain.aggregate.PricingRule;
import org.labcabrera.parking.pricing.domain.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PricingRuleService {

    private final PricingRuleRepository repository;

    @Transactional
    public PricingRule create(PricingRule pricingRule) {
        return repository.save(pricingRule);
    }

    @Transactional(readOnly = true)
    public PricingRule get(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pricing rule not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PricingRule> list(UUID facilityId, Pageable pageable) {
        if (facilityId != null) {
            return repository.findByFacilityId(facilityId, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    public PricingRule update(UUID id, PricingRule source) {
        PricingRule existing = get(id);
        existing.update(
            source.getName(),
            source.getBillingType(),
            source.getBaseRate(),
            source.getHourlyRate(),
            source.getDailyRate(),
            source.getTaxRate(),
            source.getStatus(),
            source.getValidFrom(),
            source.getValidTo());
        return repository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Pricing rule not found: " + id);
        }
        repository.deleteById(id);
    }
}
