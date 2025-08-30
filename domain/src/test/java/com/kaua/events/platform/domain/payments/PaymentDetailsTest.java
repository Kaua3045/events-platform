package com.kaua.events.platform.domain.payments;

import com.kaua.events.platform.domain.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PaymentDetailsTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewCreditCardPaymentDetails_thenInstantiate() {
        final var aAmount = BigDecimal.valueOf(10);

        final var aCreditCardDetails = new CreditCardPaymentDetails(
                aAmount,
                "John Doe",
                "123.456.789-00",
                "john.doe@mail.com",
                "120834182789",
                1
        );

        Assertions.assertEquals(aAmount, aCreditCardDetails.amount());
        Assertions.assertEquals(PaymentMethod.CREDIT_CARD, aCreditCardDetails.method());
    }

    @Test
    void givenAValidValues_whenCallNewPixPaymentDetails_thenInstantiate() {
        final var aAmount = BigDecimal.valueOf(10);

        final var aPixDetails = new PixPaymentDetails(aAmount);

        Assertions.assertEquals(aAmount, aPixDetails.amount());
        Assertions.assertEquals(PaymentMethod.PIX, aPixDetails.method());
    }
}
