package com.kaua.events.platform.infrastructure.payments;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

class PaymentJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(paymentRepository());
    }

    @Test
    void givenAValidNewPayment_whenCallSave_thenPaymentIsPersisted() {
        Assertions.assertEquals(0, countPayments());

        final var aOrderId = new OrderID(ULID.random());
        final var aAmount = BigDecimal.valueOf(250.00);
        final var aMethod = PaymentMethod.PIX;

        final var aPayment = Payment.newPayment(
                aOrderId,
                aMethod,
                aAmount
        );

        final var aActualPayment = this.paymentRepository().save(aPayment);

        Assertions.assertEquals(1, countPayments());
        Assertions.assertEquals(aPayment.getId(), aActualPayment.getId());
        Assertions.assertEquals(aPayment.getVersion(), aActualPayment.getVersion());
        Assertions.assertEquals(aPayment.getOrderId(), aActualPayment.getOrderId());
        Assertions.assertEquals(aPayment.getTransactionId(), aActualPayment.getTransactionId());
        Assertions.assertEquals(aPayment.getAmount(), aActualPayment.getAmount());
        Assertions.assertEquals(aPayment.getMethod(), aActualPayment.getMethod());
        Assertions.assertEquals(aPayment.getStatus(), aActualPayment.getStatus());
        Assertions.assertEquals(aPayment.getCreatedAt(), aActualPayment.getCreatedAt());
        Assertions.assertEquals(aPayment.getUpdatedAt(), aActualPayment.getUpdatedAt());
        Assertions.assertEquals(aPayment.getExpiresIn(), aActualPayment.getExpiresIn());
    }

    @Test
    void givenAValidPersistedPayment_whenCallSave_thenReturnUpdatedPayment() {
        Assertions.assertEquals(0, countPayments());

        final var aPayment = Fixture.PaymentFixture.newPayment();

        this.paymentRepository().save(aPayment);

        final var aUpdatedPayment = aPayment.markAsPending(
                3600,
                "qrCodeCopyAndPaste",
                "qrCodeUrlImage"
        );

        final var aActualPayment = this.paymentRepository().save(aUpdatedPayment);

        Assertions.assertEquals(aPayment.getId(), aActualPayment.getId());
        Assertions.assertEquals(aPayment.getVersion() + 1, aActualPayment.getVersion());
        Assertions.assertEquals(aUpdatedPayment.getTransactionId(), aActualPayment.getTransactionId());
        Assertions.assertEquals(aUpdatedPayment.getStatus(), aActualPayment.getStatus());
        Assertions.assertEquals(aUpdatedPayment.getMethod(), aActualPayment.getMethod());
        Assertions.assertEquals(aUpdatedPayment.getAmount(), aActualPayment.getAmount());
        Assertions.assertEquals(aUpdatedPayment.getQrCode().get(), aActualPayment.getQrCode().get());
        Assertions.assertEquals(aUpdatedPayment.getQrCodeImageUrl().get(), aActualPayment.getQrCodeImageUrl().get());
        Assertions.assertTrue(aUpdatedPayment.getPaidAt().isEmpty());
        Assertions.assertTrue(aActualPayment.getUpdatedAt().isAfter(aUpdatedPayment.getCreatedAt()));
    }

    @Test
    void givenAValidPaymentButVersionMismatch_whenCallSave_thenThrowsConflictException() {
        Assertions.assertEquals(0, countPayments());

        final var aPayment = Fixture.PaymentFixture.newPayment();

        this.paymentRepository().save(aPayment);

        final var aUpdatedPayment = aPayment.markAsPending(
                3600,
                "qrCodeCopyAndPaste",
                "qrCodeUrlImage"
        );
        aUpdatedPayment.incrementVersion();

        final var expectedMessage = "Payment with identifier %s and version %d does not match, payment was updated by another transaction"
                .formatted(aUpdatedPayment.getId().value(), aUpdatedPayment.getVersion());

        final var aException = Assertions.assertThrows(
                ConflictException.class,
                () -> this.paymentRepository().save(aUpdatedPayment)
        );

        Assertions.assertEquals(expectedMessage, aException.getMessage());
    }

    @Test
    void givenAPersistedPayment_whenCallPaymentOfOrderId_thenReturnPayment() {
        Assertions.assertEquals(0, countPayments());

        final var aPayment = Fixture.PaymentFixture.newPayment();
        this.paymentRepository().save(aPayment);

        final var maybePayment = this.paymentRepository().paymentOfOrderId(aPayment.getOrderId().value().toString());

        Assertions.assertTrue(maybePayment.isPresent());
        final var actualPayment = maybePayment.get();

        Assertions.assertEquals(aPayment.getId(), actualPayment.getId());
        Assertions.assertEquals(aPayment.getVersion(), actualPayment.getVersion());
        Assertions.assertEquals(aPayment.getOrderId(), actualPayment.getOrderId());
        Assertions.assertEquals(aPayment.getTransactionId(), actualPayment.getTransactionId());
        Assertions.assertEquals(aPayment.getAmount().setScale(2, RoundingMode.HALF_UP), actualPayment.getAmount());
        Assertions.assertEquals(aPayment.getMethod(), actualPayment.getMethod());
        Assertions.assertEquals(aPayment.getStatus(), actualPayment.getStatus());
        Assertions.assertEquals(aPayment.getCreatedAt(), actualPayment.getCreatedAt());
        Assertions.assertEquals(aPayment.getUpdatedAt(), actualPayment.getUpdatedAt());
        Assertions.assertEquals(aPayment.getExpiresIn(), actualPayment.getExpiresIn());
    }

    @Test
    void givenNoPaymentForOrderId_whenCallPaymentOfOrderId_thenReturnEmpty() {
        Assertions.assertEquals(0, countPayments());

        final var maybePayment = this.paymentRepository().paymentOfOrderId("non-existent-order-id");

        Assertions.assertTrue(maybePayment.isEmpty());
    }
}
