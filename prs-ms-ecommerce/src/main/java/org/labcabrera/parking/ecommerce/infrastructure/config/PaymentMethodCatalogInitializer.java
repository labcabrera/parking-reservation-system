package org.labcabrera.parking.ecommerce.infrastructure.config;

import java.util.List;

import org.labcabrera.parking.ecommerce.application.port.PaymentMethodRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.PaymentMethod;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PaymentMethodCatalogInitializer implements ApplicationRunner {

    private static final String MOCK_GATEWAY = "mock-payment-gateway";

    private final PaymentMethodRepository repository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        defaultMethods().stream()
            .filter(paymentMethod -> !repository.existsByCode(paymentMethod.getCode()))
            .forEach(repository::save);
    }

    private List<PaymentMethod> defaultMethods() {
        return List.of(
            PaymentMethod.create(
                new PaymentMethodCode("VISA"),
                "Visa",
                PaymentMethodType.CARD,
                MOCK_GATEWAY,
                "/assets/payment-icons/visa.svg",
                10),
            PaymentMethod.create(
                new PaymentMethodCode("MASTERCARD"),
                "Mastercard",
                PaymentMethodType.CARD,
                MOCK_GATEWAY,
                "/assets/payment-icons/mastercard.svg",
                20),
            PaymentMethod.create(
                new PaymentMethodCode("PAYPAL"),
                "PayPal",
                PaymentMethodType.WALLET,
                MOCK_GATEWAY,
                "/assets/payment-icons/paypal.svg",
                30));
    }
}
