package com.kaua.events.platform.infrastructure.payments;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.payments.CreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

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
                new PixPaymentDetails(
                        aAmount,
                        "qrCode",
                        "qrCodeImageUrl",
                        1000
                ),
                aAmount
        );

        final var aActualPayment = this.paymentRepository().save(aPayment);

        Assertions.assertEquals(1, countPayments());
        Assertions.assertEquals(aPayment.getId(), aActualPayment.getId());
        Assertions.assertEquals(aPayment.getVersion(), aActualPayment.getVersion());
        Assertions.assertEquals(aPayment.getOrderId(), aActualPayment.getOrderId());
        Assertions.assertEquals(aPayment.getTransactionId(), aActualPayment.getTransactionId());
        Assertions.assertEquals(aPayment.getAmount(), aActualPayment.getAmount());
        Assertions.assertEquals(aPayment.getPaymentDetails(), aActualPayment.getPaymentDetails());
        Assertions.assertEquals(aPayment.getMethod(), aActualPayment.getMethod());
        Assertions.assertEquals(aPayment.getStatus(), aActualPayment.getStatus());
        Assertions.assertEquals(aPayment.getCreatedAt(), aActualPayment.getCreatedAt());
        Assertions.assertEquals(aPayment.getUpdatedAt(), aActualPayment.getUpdatedAt());
    }

    @Test
    void givenAValidPersistedPayment_whenCallSave_thenReturnUpdatedPayment() {
        Assertions.assertEquals(0, countPayments());

        final var aPayment = Fixture.PaymentFixture.newPayment();

        this.paymentRepository().save(aPayment);

        final var aUpdatedPayment = aPayment.markAsPending(
                new PixPaymentDetails(
                        aPayment.getAmount(),
                        "qrCode",
                        "qrCodeImageUrl",
                        1000
                )
        );

        final var aActualPayment = this.paymentRepository().save(aUpdatedPayment);

        Assertions.assertEquals(aPayment.getId(), aActualPayment.getId());
        Assertions.assertEquals(aPayment.getVersion() + 1, aActualPayment.getVersion());
        Assertions.assertEquals(aUpdatedPayment.getTransactionId(), aActualPayment.getTransactionId());
        Assertions.assertEquals(aUpdatedPayment.getStatus(), aActualPayment.getStatus());
        Assertions.assertEquals(aUpdatedPayment.getMethod(), aActualPayment.getMethod());
        Assertions.assertEquals(aUpdatedPayment.getAmount(), aActualPayment.getAmount());
        Assertions.assertTrue(aUpdatedPayment.getPaidAt().isEmpty());
        Assertions.assertTrue(aActualPayment.getUpdatedAt().isAfter(aUpdatedPayment.getCreatedAt()));
    }

    @Test
    void givenAValidPaymentButVersionMismatch_whenCallSave_thenThrowsConflictException() {
        Assertions.assertEquals(0, countPayments());

        final var aPayment = Fixture.PaymentFixture.newPayment();

        this.paymentRepository().save(aPayment);

        final var aUpdatedPayment = aPayment.markAsPending(
                new PixPaymentDetails(
                        aPayment.getAmount(),
                        "qrCode",
                        "qrCodeImageUrl",
                        1000
                )
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
    }

    @Test
    void givenNoPaymentForOrderId_whenCallPaymentOfOrderId_thenReturnEmpty() {
        Assertions.assertEquals(0, countPayments());

        final var maybePayment = this.paymentRepository().paymentOfOrderId("non-existent-order-id");

        Assertions.assertTrue(maybePayment.isEmpty());
    }

    @Test
    void givenAValidNewCreditCardPayment_whenCallSave_thenPaymentIsPersisted() {
        Assertions.assertEquals(0, countPayments());

        final var aOrderId = new OrderID(ULID.random());
        final var aAmount = BigDecimal.valueOf(500.00);
        final var aMethod = PaymentMethod.CREDIT_CARD;

        final var aPayment = Payment.newPayment(
                aOrderId,
                aMethod,
                new CreditCardPaymentDetails(
                        aAmount,
                        "valid-token",
                        1,
                        IdentifierUtils.generateNewULID().toString()
                ),
                aAmount
        );

        final var aActualPayment = this.paymentRepository().save(aPayment);

        Assertions.assertEquals(1, countPayments());
        Assertions.assertEquals(aPayment.getId(), aActualPayment.getId());
        Assertions.assertEquals(aPayment.getVersion(), aActualPayment.getVersion());
        Assertions.assertEquals(aPayment.getOrderId(), aActualPayment.getOrderId());
        Assertions.assertEquals(aPayment.getTransactionId(), aActualPayment.getTransactionId());
        Assertions.assertEquals(aPayment.getAmount(), aActualPayment.getAmount());
        Assertions.assertEquals(aPayment.getPaymentDetails(), aActualPayment.getPaymentDetails());
        Assertions.assertEquals(PaymentMethod.CREDIT_CARD, aActualPayment.getMethod());
        Assertions.assertEquals(aPayment.getStatus(), aActualPayment.getStatus());
        Assertions.assertEquals(aPayment.getCreatedAt(), aActualPayment.getCreatedAt());
        Assertions.assertEquals(aPayment.getUpdatedAt(), aActualPayment.getUpdatedAt());
    }

    @Test
    void givenAPersistedPaymentWithCreditCardDetails_whenCallPaymentOfOrderId_thenReturnPayment() {
        Assertions.assertEquals(0, countPayments());

        final var aPayment = Payment.newPayment(
                new OrderID(ULID.random()),
                PaymentMethod.CREDIT_CARD,
                new CreditCardPaymentDetails(
                        new BigDecimal("10"),
                        "valid-token",
                        1,
                        IdentifierUtils.generateNewULID().toString()
                ),
                new BigDecimal("10")
        );
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
    }

    @Test
    @Sql(statements = {
            "INSERT INTO payments (id, version, order_id, transaction_id, status, method, amount, created_at, updated_at) " +
                    "VALUES ('01HZZZZZZZZZZZZZZZZZZZZZZZ', 0, '01HYYYYYYYYYYYYYYYYYYYYYYY', 'tx_invalid', 'PENDING', 'INVALID_METHOD', 100.00, NOW(), NOW())",
            "INSERT INTO payment_details (id, version, qr_code, qr_code_image_url, expires_in, payment_token, installments) " +
                    "VALUES ('01HZZZZZZZZZZZZZZZZZZZZZZZ', 0, NULL, NULL, 0, NULL, 0)"
    })
    void givenInvalidPaymentMethodInDatabase_whenCallPaymentOfOrderId_thenReturnPaymentWithoutDetails() {
        final var expectedErrorMessage = "should not be null";
        final var expectedErrorProperty = "method";

        final var aException = Assertions.assertThrows(ValidationException.class, () -> this.paymentRepository()
                .paymentOfOrderId("01HYYYYYYYYYYYYYYYYYYYYYYY"));

        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, aException.getErrors().getFirst().property());
    }
}
