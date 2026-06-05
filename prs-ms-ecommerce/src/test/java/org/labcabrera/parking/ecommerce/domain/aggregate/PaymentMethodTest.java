package org.labcabrera.parking.ecommerce.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodCode;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodStatus;
import org.labcabrera.parking.ecommerce.domain.valueobject.PaymentMethodType;

class PaymentMethodTest {

    @Test
    void createsActivePaymentMethodByDefault() {
        PaymentMethod paymentMethod = PaymentMethod.create(
            new PaymentMethodCode("VISA"),
            "Visa",
            PaymentMethodType.CARD,
            "mock-gateway",
            "/assets/payment-icons/visa.svg",
            10);

        assertThat(paymentMethod.getId()).isNotNull();
        assertThat(paymentMethod.getStatus()).isEqualTo(PaymentMethodStatus.ACTIVE);
        assertThat(paymentMethod.isAvailableForCheckout()).isTrue();
        assertThat(paymentMethod.getCreatedAt()).isNotNull();
    }

    @Test
    void inactivePaymentMethodIsNotAvailableForCheckout() {
        PaymentMethod paymentMethod = PaymentMethod.create(
            new PaymentMethodCode("PAYPAL"),
            "PayPal",
            PaymentMethodType.WALLET,
            "mock-gateway",
            null,
            20);

        paymentMethod.update(
            "PayPal",
            PaymentMethodType.WALLET,
            PaymentMethodStatus.INACTIVE,
            "mock-gateway",
            null,
            20);

        assertThat(paymentMethod.isAvailableForCheckout()).isFalse();
        assertThat(paymentMethod.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsMissingGatewayProvider() {
        assertThatThrownBy(() -> PaymentMethod.create(
            new PaymentMethodCode("BIZUM"),
            "Bizum",
            PaymentMethodType.WALLET,
            " ",
            null,
            30))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("payment method gateway provider is required");
    }
}
