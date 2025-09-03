package com.kaua.events.platform.application.usecases.payments.process.charges;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.PaymentStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

class ProcessPaymentChargeUseCaseTest extends UseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private DefaultProcessPaymentChargeUseCase useCase;

    @Test
    void givenNullInput_whenExecute_thenThrowUseCaseInputCannotBeNullException() {
        final var exception = Assertions.assertThrows(
                UseCaseInputCannotBeNullException.class,
                () -> useCase.execute(null)
        );

        Assertions.assertEquals("Input to ProcessPaymentChargeUseCase cannot be null", exception.getMessage());
    }

    @Test
    void givenEmptyNotifications_whenExecute_thenDoNothing() {
        final var input = ProcessPaymentChargeInput.with("notif-id-1");

        final var expectedErrorMessage = "No payment notifications found for id: notif-id-1";

        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, List.of()));

        final var aException = Assertions.assertThrows(DomainException.class, () -> useCase.execute(input));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void givenMultipleNotifications_prioritizeUnpaidOverOthers() {
        final var payment = Fixture.PaymentFixture.newPayment();
        final var notifications = List.of(
                createNotificationData(payment, "paid", null),
                createNotificationData(payment, "unpaid", null),
                createNotificationData(payment, "approved", null)
        );

        final var input = ProcessPaymentChargeInput.with("notif-id-1");
        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> useCase.execute(input));
        verify(paymentRepository, times(1)).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED));
    }

    @Test
    void givenMultipleNotifications_prioritizePaidOverApprovedIdentified() {
        final var payment = Fixture.PaymentFixture.newPayment();
        final var notifications = List.of(
                createNotificationData(payment, "approved", null),
                createNotificationData(payment, "paid", null),
                createNotificationData(payment, "identified", null)
        );

        final var input = ProcessPaymentChargeInput.with("notif-id-2");
        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> useCase.execute(input));
        verify(paymentRepository, times(1)).save(argThat(p -> p.getStatus() == PaymentStatus.PAID));
    }

    @Test
    void givenApprovedAndIdentifiedNotifications_whenExecute_thenMarkAsApproved() {
        final var payment = Fixture.PaymentFixture.newPayment();
        final var notifications = List.of(
                createNotificationData(payment, "identified", null),
                createNotificationData(payment, "approved", null)
        );

        final var input = ProcessPaymentChargeInput.with("notif-id-3");
        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> useCase.execute(input));
        verify(paymentRepository, times(1)).save(argThat(p -> p.getStatus() == PaymentStatus.APPROVED));
    }

    @Test
    void givenIdentifiedNotifications_whenExecute_thenMarkAsIdentified() {
        final var payment = Fixture.PaymentFixture.newPayment();
        final var notifications = List.of(
                createNotificationData(payment, "identified", null)
        );

        final var input = ProcessPaymentChargeInput.with("notif-id-3");
        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> useCase.execute(input));
        verify(paymentRepository, times(1)).save(argThat(p -> p.getStatus() == PaymentStatus.IDENTIFIED));
    }

    @Test
    void givenRefundedNotification_whenExecute_thenThrowDomainException() {
        final var payment = Fixture.PaymentFixture.newPayment();
        final var notifications = List.of(
                createNotificationData(payment, "refunded", null),
                createNotificationData(payment, "paid", null)
        );

        final var input = ProcessPaymentChargeInput.with("notif-id-4");
        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.of(payment));

        Assertions.assertThrows(DomainException.class, () -> useCase.execute(input));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void givenPaymentNotFound_whenExecute_thenThrowNotFoundException() {
        final var notifications = List.of(
                new PaymentGateway.PaymentNotificationData(
                        1, "type", "non-existent-order", "paid", 1, "2024-01-01T00:00:00Z"
                )
        );
        final var input = ProcessPaymentChargeInput.with("notif-id-5");

        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.empty());

        final var exception = Assertions.assertThrows(NotFoundException.class,
                () -> useCase.execute(input));

        Assertions.assertTrue(exception.getMessage().contains("Payment with orderId non-existent-order was not found"));
    }

    @Test
    void givenUnknownStatusNotification_whenExecute_thenThrowDomainException() {
        final var payment = Fixture.PaymentFixture.newPayment();
        final var notifications = List.of(
                createNotificationData(payment, "waiting", null)
        );

        final var input = ProcessPaymentChargeInput.with("notif-id-unknown");
        when(paymentGateway.getNotifications(input.notificationId()))
                .thenReturn(new PaymentGateway.PaymentNotification(200, notifications));
        when(paymentRepository.paymentOfOrderId(anyString())).thenReturn(Optional.of(payment));

        final var exception = Assertions.assertThrows(DomainException.class,
                () -> useCase.execute(input));

        Assertions.assertEquals("Unknown payment status: waiting", exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }


    private PaymentGateway.PaymentNotificationData createNotificationData(
            Payment payment, String status, String customId
    ) {
        return new PaymentGateway.PaymentNotificationData(
                1, "type", customId != null ? customId : payment.getOrderId().value().toString(),
                status, 1, "2024-01-01T00:00:00Z"
        );
    }
}
