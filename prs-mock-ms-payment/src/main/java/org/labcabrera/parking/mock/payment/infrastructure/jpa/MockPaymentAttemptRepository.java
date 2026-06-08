package org.labcabrera.parking.mock.payment.infrastructure.jpa;

import java.util.UUID;

import org.labcabrera.parking.mock.payment.infrastructure.jpa.entities.MockPaymentAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MockPaymentAttemptRepository extends JpaRepository<MockPaymentAttemptEntity, UUID> {
}
