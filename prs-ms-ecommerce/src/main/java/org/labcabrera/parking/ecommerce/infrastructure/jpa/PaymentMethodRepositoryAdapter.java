package org.labcabrera.parking.ecommerce.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.application.port.PaymentMethodRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentMethod;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final PaymentMethodJpaRepository repository;
    private final PaymentMethodJpaMapper mapper;

    @Override
    @Transactional
    public PaymentMethod save(PaymentMethod paymentMethod) {
        return mapper.toDomain(repository.save(mapper.toEntity(paymentMethod)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentMethod> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentMethod> findByCode(PaymentMethodCode code) {
        return repository.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethod> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethod> findActive(Pageable pageable) {
        return repository.findByStatus(PaymentMethodStatus.ACTIVE, pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(PaymentMethodCode code) {
        return repository.existsByCode(code.value());
    }
}
