package com.kaua.events.platform.application.usecases.payments.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.domain.payments.CreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Objects;

import static com.kaua.events.platform.application.gateways.PaymentGateway.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class CreatePaymentUseCaseTest extends UseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private DefaultCreatePaymentUseCase useCase;

    @Test
    void givenAValidPixPayment_whenCallCreatePaymentUseCase_thenReturnOutput() {
        final var aDetails = new PixPaymentDetails(BigDecimal.TEN);
        final var aOrderId = ULID.random().toString();
        final var aInput = CreatePaymentInput.with(aDetails, aOrderId, "trace-123");

        final var aQrCode = "qr-code-123";
        final var aQrCodeImageUrl = "http://image.test/qr.png";
        final var aExpiresIn = 3600;

        Mockito.when(paymentRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        Mockito.when(paymentGateway.process(Mockito.any(PaymentProcessRequest.class)))
                .thenReturn(new PaymentProcessResponse(
                        aQrCode,
                        aQrCodeImageUrl,
                        aExpiresIn,
                        PaymentProcessStatus.ACTIVE
                ));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.paymentId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("createPaymentUseCase"), Mockito.any());
        Mockito.verify(paymentRepository, Mockito.times(2))
                .save(argThat(aPayment ->
                        Objects.equals(aDetails.method(), aPayment.getMethod())
                                && Objects.equals(aDetails.amount(), aPayment.getAmount())));
        Mockito.verify(paymentGateway, Mockito.times(1))
                .process(Mockito.any(PaymentProcessRequest.class));
    }

    @Test
    void givenAValidPixPaymentWithNonActiveStatus_whenCallCreatePaymentUseCase_thenThrowRuntimeException() {
        final var aDetails = new PixPaymentDetails(BigDecimal.ONE);
        final var aOrderId = ULID.random().toString();
        final var aInput = CreatePaymentInput.with(aDetails, aOrderId, "trace-xyz");

        Mockito.when(paymentRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        Mockito.when(paymentGateway.process(Mockito.any(PaymentProcessRequest.class)))
                .thenReturn(new PaymentProcessResponse(
                        null,
                        null,
                        0,
                        PaymentProcessStatus.FAILED
                ));

        final var aException = Assertions.assertThrows(RuntimeException.class, () -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aException);

        Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
        Mockito.verify(paymentGateway, Mockito.times(1)).process(Mockito.any(PaymentProcessRequest.class));
    }

    @Test
    void givenAnCreditCardPaymentDetails_whenCallCreatePaymentUseCase_thenReturnOutput() {
        final var aDetails = new CreditCardPaymentDetails(
                BigDecimal.valueOf(100),
                "John Doe",
                "123.456.789-00",
                "+55 (11) 91234-5678",
                "john.doe@mail.com",
                "120834182789",
                1
        );
        final var aOrderId = ULID.random().toString();
        final var aInput = CreatePaymentInput.with(aDetails, aOrderId, "trace-abc");

        Mockito.when(paymentRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg())
                .thenAnswer(returnsFirstArg());
        Mockito.when(paymentGateway.process(Mockito.any(PaymentProcessRequest.class)))
                .thenReturn(new PaymentProcessResponse(
                        null,
                        null,
                        0,
                        PaymentProcessStatus.WAITING
                ));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.paymentId());

        Mockito.verify(paymentRepository, Mockito.times(2)).save(Mockito.any(Payment.class));
        Mockito.verify(paymentGateway, Mockito.times(1)).process(Mockito.any(PaymentProcessRequest.class));
    }

    @Test
    void givenANullInput_whenCallCreatePaymentUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var expectedMessage = "Input to CreatePaymentUseCase cannot be null";

        final var actualException = Assertions.assertThrows(
                UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null)
        );

        Assertions.assertEquals(expectedMessage, actualException.getMessage());

        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(paymentGateway, Mockito.never()).process(Mockito.any());
    }
}
