package com.kaua.events.platform.domain.payments;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PaymentTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewPayment_thenInstantiatePayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());

        final var aPayment = Payment.newPayment(aOrderId, aPaymentMethod, aAmount);

        Assertions.assertNotNull(aPayment.getId());
        Assertions.assertNotNull(aPayment.getTransactionId());
        Assertions.assertEquals(aOrderId.value(), aPayment.getOrderId().value());
        Assertions.assertEquals(PaymentStatus.NEW, aPayment.getStatus());
        Assertions.assertEquals(aPaymentMethod, aPayment.getMethod());
        Assertions.assertTrue(aPayment.getQrCode().isEmpty());
        Assertions.assertTrue(aPayment.getQrCodeImageUrl().isEmpty());
        Assertions.assertNotNull(aPayment.getCreatedAt());
        Assertions.assertNotNull(aPayment.getUpdatedAt());
        Assertions.assertTrue(aPayment.getPaidAt().isEmpty());
        Assertions.assertEquals(0, aPayment.getExpiresIn());
        Assertions.assertDoesNotThrow(() -> aPayment.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallWithPayment_thenInstantiatePayment() {
        final var aPaymentId = new PaymentID(ULID.random());
        final var aVersion = 0L;
        final var aOrderId = new OrderID(ULID.random());
        final var aTransactionId = IdentifierUtils.generateNewIdWithoutHyphen();
        final var aPaymentStatus = PaymentStatus.NEW;
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aQrCode = "copy-and-past";
        final var aQrCodeImageUrl = "http://qrcode-image";
        final var aNow = InstantUtils.now();
        final var aExpiresIn = 0;

        final var aPayment = Payment.with(
                aPaymentId,
                aVersion,
                aOrderId,
                aTransactionId,
                aPaymentStatus,
                aPaymentMethod,
                aAmount,
                aQrCode,
                aQrCodeImageUrl,
                aNow,
                aNow,
                null,
                aExpiresIn
        );

        Assertions.assertEquals(aPaymentId, aPayment.getId());
        Assertions.assertEquals(aOrderId.value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aTransactionId, aPayment.getTransactionId());
        Assertions.assertEquals(aPaymentStatus, aPayment.getStatus());
        Assertions.assertEquals(aPaymentMethod, aPayment.getMethod());
        Assertions.assertEquals(aQrCode, aPayment.getQrCode().get());
        Assertions.assertEquals(aQrCodeImageUrl, aPayment.getQrCodeImageUrl().get());
        Assertions.assertEquals(aNow, aPayment.getCreatedAt());
        Assertions.assertEquals(aNow, aPayment.getUpdatedAt());
        Assertions.assertTrue(aPayment.getPaidAt().isEmpty());
        Assertions.assertEquals(aExpiresIn, aPayment.getExpiresIn());
    }

    @Test
    void givenAValidValues_whenCallMarkAsPending_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());

        final var aQrCode = "copy-and-past";
        final var aQrCodeImageUrl = "http://qrcode-image";
        final var aExpiresIn = 10;

        final var aPayment = Payment.newPayment(aOrderId, aPaymentMethod, aAmount);

        final var aPendingPayment = aPayment.markAsPending(
                aExpiresIn,
                aQrCode,
                aQrCodeImageUrl
        );

        Assertions.assertEquals(aPendingPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aPendingPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aPendingPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.PENDING, aPendingPayment.getStatus());
        Assertions.assertEquals(aPendingPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aQrCode, aPendingPayment.getQrCode().get());
        Assertions.assertEquals(aQrCodeImageUrl, aPendingPayment.getQrCodeImageUrl().get());
        Assertions.assertEquals(aPendingPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aPendingPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aPayment.getPaidAt().isEmpty());
        Assertions.assertEquals(aExpiresIn, aPendingPayment.getExpiresIn());
    }

    @Test
    void givenAValidPayment_whenCallMarkAsPaid_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());

        final var aPayment = Payment.newPayment(aOrderId, aPaymentMethod, aAmount);

        final var aPaidPayment = aPayment.markAsPaid();

        Assertions.assertEquals(aPaidPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aPaidPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aPaidPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.PAID, aPaidPayment.getStatus());
        Assertions.assertEquals(aPaidPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aPaidPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aPaidPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aPaidPayment.getPaidAt().isPresent());
        Assertions.assertEquals(aPaidPayment.getExpiresIn(), aPayment.getExpiresIn());
    }

    @Test
    void testCallToStringInPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());

        final var aPayment = Payment.newPayment(aOrderId, aPaymentMethod, aAmount);

        final var aPaymentToString = aPayment.toString();

        Assertions.assertNotNull(aPaymentToString);
    }

    @Test
    void givenAValidValue_whenCallPaymentStatusFrom_thenReturnPaymentStatus() {
        final var aPaymentStatus = PaymentStatus.from("NEW");

        Assertions.assertNotNull(aPaymentStatus);
        Assertions.assertEquals(PaymentStatus.NEW, aPaymentStatus.get());
    }

    @Test
    void givenAnInvalidValue_whenCallPaymentStatusFrom_thenReturnEmpty() {
        final var aPaymentStatus = PaymentStatus.from("INVALID");

        Assertions.assertTrue(aPaymentStatus.isEmpty());
    }
}
