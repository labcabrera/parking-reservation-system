package org.labcabrera.parking.ecommerce.application.port;

import java.util.Optional;
import java.util.UUID;

import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentMethod;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentMethodRepository {

    PaymentMethod save(PaymentMethod paymentMethod);

    Optional<PaymentMethod> findById(UUID id);

    Optional<PaymentMethod> findByCode(PaymentMethodCode code);

    Page<PaymentMethod> findAll(Pageable pageable);

    Page<PaymentMethod> findActive(Pageable pageable);

    boolean existsByCode(PaymentMethodCode code);
}
