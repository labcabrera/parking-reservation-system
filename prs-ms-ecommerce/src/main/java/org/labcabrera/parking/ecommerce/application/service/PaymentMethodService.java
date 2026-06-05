package org.labcabrera.parking.ecommerce.application.service;

import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.PaymentMethodRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentMethod;
import org.labcabrera.parking.ecommerce.domain.exception.DomainException;
import org.labcabrera.parking.ecommerce.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository repository;

    @Transactional
    public PaymentMethod create(PaymentMethod paymentMethod) {
        if (repository.existsByCode(paymentMethod.getCode())) {
            throw new DomainException("Payment method already exists: " + paymentMethod.getCode().value());
        }
        return repository.save(paymentMethod);
    }

    @Transactional(readOnly = true)
    public PaymentMethod get(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment method not found: " + id));
    }

    @Transactional(readOnly = true)
    public PaymentMethod getByCode(PaymentMethodCode code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Payment method not found: " + code.value()));
    }

    @Transactional(readOnly = true)
    public Page<PaymentMethod> list(Pageable pageable, boolean onlyActive) {
        return onlyActive ? repository.findActive(pageable) : repository.findAll(pageable);
    }

    @Transactional
    public PaymentMethod update(UUID id, PaymentMethod source) {
        PaymentMethod existing = get(id);
        existing.update(
            source.getDisplayName(),
            source.getType(),
            source.getStatus(),
            source.getGatewayProvider(),
            source.getIconUrl(),
            source.getDisplayOrder());
        return repository.save(existing);
    }
}
