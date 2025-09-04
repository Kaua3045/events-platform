package com.kaua.events.platform.domain.payments;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.payments.events.PaymentCreatedEvent;
import com.kaua.events.platform.domain.payments.events.PaymentStatusChangedEvent;
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
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        Assertions.assertNotNull(aPayment.getId());
        Assertions.assertNotNull(aPayment.getTransactionId());
        Assertions.assertEquals(aOrderId.value(), aPayment.getOrderId().value());
        Assertions.assertEquals(PaymentStatus.NEW, aPayment.getStatus());
        Assertions.assertEquals(aPaymentMethod, aPayment.getMethod());
        Assertions.assertEquals(aPaymentDetails, aPayment.getPaymentDetails());
        Assertions.assertEquals(aPaymentDetails.getExpiresIn(), ((PixPaymentDetails) aPayment.getPaymentDetails()).getExpiresIn());
        Assertions.assertTrue(((PixPaymentDetails) aPayment.getPaymentDetails()).getQrCode().isEmpty());
        Assertions.assertTrue(((PixPaymentDetails) aPayment.getPaymentDetails()).getQrCodeImageUrl().isEmpty());
        Assertions.assertNotNull(aPayment.getCreatedAt());
        Assertions.assertNotNull(aPayment.getUpdatedAt());
        Assertions.assertTrue(aPayment.getPaidAt().isEmpty());
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

        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                aQrCode,
                aQrCodeImageUrl,
                aExpiresIn
        );

        final var aPayment = Payment.with(
                aPaymentId,
                aVersion,
                aOrderId,
                aTransactionId,
                aPaymentStatus,
                aPaymentMethod,
                aAmount,
                aPaymentDetails,
                aNow,
                aNow,
                null
        );

        Assertions.assertEquals(aPaymentId, aPayment.getId());
        Assertions.assertEquals(aOrderId.value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aTransactionId, aPayment.getTransactionId());
        Assertions.assertEquals(aPaymentStatus, aPayment.getStatus());
        Assertions.assertEquals(aPaymentMethod, aPayment.getMethod());
        Assertions.assertEquals(aPaymentDetails, aPayment.getPaymentDetails());
        Assertions.assertEquals(aNow, aPayment.getCreatedAt());
        Assertions.assertEquals(aNow, aPayment.getUpdatedAt());
        Assertions.assertTrue(aPayment.getPaidAt().isEmpty());
    }

    @Test
    void givenAValidValues_whenCallMarkAsPending_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aQrCode = "copy-and-past";
        final var aQrCodeImageUrl = "http://qrcode-image";
        final var aExpiresIn = 10;

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aPendingPayment = aPayment.markAsPending(aPaymentDetails);

        Assertions.assertEquals(aPendingPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aPendingPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aPendingPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.PENDING, aPendingPayment.getStatus());
        Assertions.assertEquals(aPendingPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aPaymentDetails, aPendingPayment.getPaymentDetails());
        Assertions.assertEquals(aPendingPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aPendingPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aPayment.getPaidAt().isEmpty());
    }

    @Test
    void givenAValidPayment_whenCallMarkAsPaid_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aPaidPayment = aPayment.markAsPaid();

        Assertions.assertEquals(aPaidPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aPaidPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aPaidPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.PAID, aPaidPayment.getStatus());
        Assertions.assertEquals(aPaidPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aPaidPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aPaidPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aPaidPayment.getPaidAt().isPresent());
    }

    @Test
    void testCallToStringInPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

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

    @Test
    void givenAValidPaymentCreatedEvent_whenCallRegisterPayment_thenReturnPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aPaymentCreatedEvent = new PaymentCreatedEvent(
                aPayment.getId().value().toString(),
                aPayment.getOrderId().value().toString(),
                aPayment.getVersion(),
                aPayment.getStatus().name(),
                aPayment.getAmount(),
                "traceId"
        );

        aPayment.registerEvent(aPaymentCreatedEvent);

        Assertions.assertEquals(1, aPayment.getDomainEvents().size());
        Assertions.assertTrue(aPayment.getDomainEvents().contains(aPaymentCreatedEvent));
    }

    @Test
    void givenAValidPayment_whenCallMarkAsApproved_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aApprovedPayment = aPayment.markAsApproved();

        Assertions.assertEquals(aApprovedPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aApprovedPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aApprovedPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.APPROVED, aApprovedPayment.getStatus());
        Assertions.assertEquals(aApprovedPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aApprovedPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aApprovedPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aApprovedPayment.getPaidAt().isEmpty());
    }

    @Test
    void givenAValidPayment_whenCallMarkAsIdentified_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aIdentifiedPayment = aPayment.markAsIdentified();

        Assertions.assertEquals(aIdentifiedPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aIdentifiedPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aIdentifiedPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.IDENTIFIED, aIdentifiedPayment.getStatus());
        Assertions.assertEquals(aIdentifiedPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aIdentifiedPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aIdentifiedPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aIdentifiedPayment.getPaidAt().isEmpty());
    }

    @Test
    void givenAValidPayment_whenCallMarkAsFailed_thenReturnUpdatedPayment() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aFailedPayment = aPayment.markAsFailed();

        Assertions.assertEquals(aFailedPayment.getId(), aPayment.getId());
        Assertions.assertEquals(aFailedPayment.getOrderId().value(), aPayment.getOrderId().value());
        Assertions.assertEquals(aFailedPayment.getTransactionId(), aPayment.getTransactionId());
        Assertions.assertEquals(PaymentStatus.FAILED, aFailedPayment.getStatus());
        Assertions.assertEquals(aFailedPayment.getMethod(), aPayment.getMethod());
        Assertions.assertEquals(aFailedPayment.getCreatedAt(), aPayment.getCreatedAt());
        Assertions.assertNotEquals(aFailedPayment.getUpdatedAt(), aPayment.getUpdatedAt());
        Assertions.assertTrue(aFailedPayment.getPaidAt().isEmpty());
    }

    @Test
    void givenAValidPaymentStatusChangedEvent_whenCallRegisterEvent_thenEventIsRegistered() {
        final var aPaymentMethod = PaymentMethod.PIX;
        final var aAmount = BigDecimal.valueOf(10);
        final var aOrderId = new OrderID(ULID.random());
        final var aPaymentDetails = new PixPaymentDetails(
                aAmount,
                null,
                null,
                0
        );

        final var aPayment = Payment.newPayment(
                aOrderId,
                aPaymentMethod,
                aPaymentDetails,
                aAmount
        );

        final var aPaymentStatusChangedEvent = new PaymentStatusChangedEvent(
                aPayment.getOrderId().value().toString(),
                aPayment.getId().value().toString(),
                aPayment.getVersion(),
                aPayment.getStatus().name(),
                "traceId"
        );

        aPayment.registerEvent(aPaymentStatusChangedEvent);

        Assertions.assertEquals(1, aPayment.getDomainEvents().size());
        Assertions.assertTrue(aPayment.getDomainEvents().contains(aPaymentStatusChangedEvent));
    }
}
