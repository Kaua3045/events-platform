package com.kaua.events.platform.application.usecases.payments.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.gateways.PaymentGateway.PaymentProcessRequest;
import com.kaua.events.platform.application.gateways.PaymentGateway.PaymentProcessStatus;
import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;

public class DefaultCreatePaymentUseCase extends CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final TracerWrapper tracerWrapper;

    public DefaultCreatePaymentUseCase(
            final PaymentRepository paymentRepository,
            final PaymentGateway paymentGateway,
            final TracerWrapper tracerWrapper
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreatePaymentOutput execute(final CreatePaymentInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createPaymentUseCase",
                ctx -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(CreatePaymentUseCase.class);

                    final var aPayment = Payment.newPayment(
                            new OrderID(ULID.fromString(input.orderId())),
                            input.paymentDetails().method(),
                            input.paymentDetails().amount()
                    );

                    ctx.runInSpan(
                            "payment.save",
                            () -> this.paymentRepository.save(aPayment)
                    );

                    final var aOutput = ctx.runInSpan(
                            "payment.process",
                            () -> this.paymentGateway.process(new PaymentProcessRequest(
                                    aPayment.getTransactionId(),
                                    aPayment.getOrderId().value().toString(),
                                    input.paymentDetails()
                            ))
                    );

                    if (aPayment.getMethod().equals(PaymentMethod.PIX) && aOutput.status().equals(PaymentProcessStatus.ACTIVE)) {
                        final var aPendingPayment = aPayment.markAsPending(
                                aOutput.expiresIn(),
                                aOutput.qrCode(),
                                aOutput.qrCodeImageUrl()
                        );

                        ctx.runInSpan(
                                "payment.update",
                                () -> this.paymentRepository.save(aPendingPayment)
                        );

                        return CreatePaymentOutput.from(aPendingPayment);
                    }

                    if (aPayment.getMethod().equals(PaymentMethod.CREDIT_CARD) && aOutput.status().equals(PaymentProcessStatus.WAITING)) {
                        final var aPendingPayment = aPayment.markAsPending(0, null, null);

                        ctx.runInSpan(
                                "payment.update",
                                () -> this.paymentRepository.save(aPendingPayment)
                        );

                        return CreatePaymentOutput.from(aPendingPayment);
                    }

                    return null;
                }
        );
    }
}
