package com.kaua.events.platform.application.usecases.payments.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.gateways.PaymentGateway.PaymentProcessRequest;
import com.kaua.events.platform.application.gateways.PaymentGateway.PaymentProcessStatus;
import com.kaua.events.platform.application.gateways.PhoneNumberGateway;
import com.kaua.events.platform.application.gateways.payment.PaymentCreditCardPaymentDetailsRequest;
import com.kaua.events.platform.application.gateways.payment.PaymentDetailsRequest;
import com.kaua.events.platform.application.gateways.payment.PaymentPixPaymentDetailsRequest;
import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.wrapper.ObservationContext;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.payments.*;
import com.kaua.events.platform.domain.payments.events.PaymentStatusChangedEvent;
import com.kaua.events.platform.domain.person.Document;
import com.kaua.events.platform.domain.users.User;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;

public class DefaultCreatePaymentUseCase extends CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final UserRepository userRepository;
    private final PhoneNumberGateway phoneNumberGateway;
    private final TracerWrapper tracerWrapper;

    public DefaultCreatePaymentUseCase(
            final PaymentRepository paymentRepository,
            final PaymentGateway paymentGateway,
            final UserRepository userRepository,
            final PhoneNumberGateway phoneNumberGateway,
            final TracerWrapper tracerWrapper
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.phoneNumberGateway = Objects.requireNonNull(phoneNumberGateway);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreatePaymentOutput execute(final CreatePaymentInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createPaymentUseCase",
                ctx -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(CreatePaymentUseCase.class);

                    final var paymentDetails = input.paymentDetails();

                    final var finalPaymentDetails = getFinalPaymentDetailsRequest(ctx, paymentDetails);

                    final var aPayment = Payment.newPayment(
                            new OrderID(ULID.fromString(input.orderId())),
                            input.paymentDetails().method(),
                            input.paymentDetails(),
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
                                    finalPaymentDetails
                            ))
                    );

                    if (aPayment.getMethod().equals(PaymentMethod.PIX) && aOutput.status().equals(PaymentProcessStatus.ACTIVE)) {
                        final var aPendingPayment = aPayment.markAsPending(
                                new PixPaymentDetails(
                                        aPayment.getAmount(),
                                        aOutput.qrCode(),
                                        aOutput.qrCodeImageUrl(),
                                        aOutput.expiresIn()
                                )
                        );

                        ctx.runInSpan(
                                "payment.update",
                                () -> this.paymentRepository.save(aPendingPayment)
                        );

                        return CreatePaymentOutput.from(aPendingPayment);
                    }

                    if (aPayment.getMethod().equals(PaymentMethod.CREDIT_CARD) && aOutput.status().equals(PaymentProcessStatus.WAITING)) {
                        final var aPendingPayment = aPayment.markAsPending(aPayment.getPaymentDetails());
                        aPendingPayment.registerEvent(new PaymentStatusChangedEvent(
                                aPendingPayment.getOrderId().value().toString(),
                                aPendingPayment.getId().value().toString(),
                                aPendingPayment.getVersion(),
                                aPendingPayment.getStatus().name(),
                                aPendingPayment.getTransactionId()
                        ));

                        ctx.runInSpan(
                                "payment.update",
                                () -> this.paymentRepository.save(aPendingPayment)
                        );

                        return CreatePaymentOutput.from(aPendingPayment);
                    }

                    throw new RuntimeException("Error on create payment " + aOutput + " " + aPayment);
                }
        );
    }

    private PaymentDetailsRequest getFinalPaymentDetailsRequest(ObservationContext ctx, PaymentDetails paymentDetails) {
        return switch (paymentDetails) {
            case CreditCardPaymentDetails creditCardDetails -> ctx.runInSpan(
                    "user.retrieve",
                    () -> this.userRepository.userOfId(creditCardDetails.userId())
                            .map(user -> new PaymentCreditCardPaymentDetailsRequest(
                                    creditCardDetails.amount(),
                                    user.getName().fullName(),
                                    user.getDocument().map(Document::value).orElse(null),
                                    user.getDocument().map(Document::type).orElse(null),
                                    user.getEmail().value(),
                                    this.phoneNumberGateway.formatToProviderBr(user.getPhoneNumber().orElse(null)),
                                    creditCardDetails.paymentToken(),
                                    creditCardDetails.installments()
                            ))
                            .orElseThrow(NotFoundException.with(User.class, creditCardDetails.userId()))
            );
            case PixPaymentDetails pixDetails -> new PaymentPixPaymentDetailsRequest(
                    pixDetails.amount()
            );
        };
    }
}
