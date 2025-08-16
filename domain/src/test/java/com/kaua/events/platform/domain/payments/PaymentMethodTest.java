package com.kaua.events.platform.domain.payments;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.orders.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentMethodTest extends UnitTest {

    @Test
    void givenAValidValue_whenCallPaymentMethodFrom_thenReturnPaymentMethod() {
        final var aPaymentMethod = PaymentMethod.from("pix");

        Assertions.assertNotNull(aPaymentMethod);
        Assertions.assertEquals(PaymentMethod.PIX, aPaymentMethod.get());
    }

    @Test
    void givenAnInvalidValue_whenCallPaymentMethodFrom_thenReturnEmpty() {
        final var aPaymentMethod = PaymentMethod.from("INVALID");

        Assertions.assertTrue(aPaymentMethod.isEmpty());
    }
}
