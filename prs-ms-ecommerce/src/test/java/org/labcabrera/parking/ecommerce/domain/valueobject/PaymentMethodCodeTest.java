package org.labcabrera.parking.ecommerce.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PaymentMethodCodeTest {

    @Test
    void normalizesCodeToUppercase() {
        PaymentMethodCode code = new PaymentMethodCode(" visa ");

        assertThat(code.value()).isEqualTo("VISA");
    }

    @Test
    void rejectsBlankCode() {
        assertThatThrownBy(() -> new PaymentMethodCode(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("payment method code is required");
    }

    @Test
    void rejectsInvalidCharacters() {
        assertThatThrownBy(() -> new PaymentMethodCode("visa-card"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("payment method code must contain only uppercase letters, digits or underscore");
    }
}
