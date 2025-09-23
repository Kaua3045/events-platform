package com.kaua.events.platform.application.usecases.payments.process.charges;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.events.PaymentStatusChangedEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultProcessPaymentChargeUseCase extends ProcessPaymentChargeUseCase {

    private static final List<String> PRIORITY = List.of("unpaid", "refunded", "paid", "approved", "identified");

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public DefaultProcessPaymentChargeUseCase(
            final PaymentRepository paymentRepository,
            final PaymentGateway paymentGateway
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
    }

    @Override
    public void execute(final ProcessPaymentChargeInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(ProcessPaymentChargeUseCase.class);

        if (input.method().equalsIgnoreCase("pix")) {
            final var aPayment = this.paymentRepository.paymentOfOrderId(input.notificationId())
                    .orElseThrow(NotFoundException.with(Payment.class, "orderId", input.notificationId()));

            final var aPaymentPaid = aPayment.markAsPaid();
            aPaymentPaid.registerEvent(new PaymentStatusChangedEvent(
                    aPaymentPaid.getOrderId().value().toString(),
                    aPaymentPaid.getId().value().toString(),
                    aPaymentPaid.getVersion(),
                    aPaymentPaid.getStatus().name(),
                    aPaymentPaid.getTransactionId()
            ));
            this.paymentRepository.save(aPaymentPaid);
            return;
        }

        final var aPaymentNotifications = this.paymentGateway.getNotifications(input.notificationId());

        if (aPaymentNotifications.data().isEmpty()) {
            throw DomainException.with("No payment notifications found for id: " + input.notificationId());
        }

        Optional<PaymentGateway.PaymentNotificationData> targetNotification = aPaymentNotifications.data().stream()
                .min(Comparator.comparingInt(n -> {
                    int idx = PRIORITY.indexOf(n.currentStatus().toLowerCase());
                    return idx >= 0 ? idx : PRIORITY.size();
                }));


        final var notification = targetNotification.get();
        final var aPayment = this.paymentRepository.paymentOfOrderId(notification.customId())
                .orElseThrow(NotFoundException.with(Payment.class, "orderId", notification.customId()));

        switch (notification.currentStatus().toLowerCase()) {
            case "paid" -> {
                final var aPaidPayment = aPayment.markAsPaid();
                aPaidPayment.registerEvent(new PaymentStatusChangedEvent(
                        aPaidPayment.getOrderId().value().toString(),
                        aPaidPayment.getId().value().toString(),
                        aPaidPayment.getVersion(),
                        aPaidPayment.getStatus().name(),
                        aPaidPayment.getTransactionId()
                ));
                this.paymentRepository.save(aPaidPayment);
            }
            case "approved" -> {
                final var aApprovedPayment = aPayment.markAsApproved();
                aApprovedPayment.registerEvent(new PaymentStatusChangedEvent(
                        aApprovedPayment.getOrderId().value().toString(),
                        aApprovedPayment.getId().value().toString(),
                        aApprovedPayment.getVersion(),
                        aApprovedPayment.getStatus().name(),
                        aApprovedPayment.getTransactionId()
                ));
                this.paymentRepository.save(aApprovedPayment);
            }
            case "identified" -> {
                final var aIdentifierPayment = aPayment.markAsIdentified();
                aIdentifierPayment.registerEvent(new PaymentStatusChangedEvent(
                        aIdentifierPayment.getOrderId().value().toString(),
                        aIdentifierPayment.getId().value().toString(),
                        aIdentifierPayment.getVersion(),
                        aIdentifierPayment.getStatus().name(),
                        aIdentifierPayment.getTransactionId()
                ));
                this.paymentRepository.save(aIdentifierPayment);
            }
            case "unpaid" -> {
                final var aUnpaidPayment = aPayment.markAsFailed();
                aUnpaidPayment.registerEvent(new PaymentStatusChangedEvent(
                        aUnpaidPayment.getOrderId().value().toString(),
                        aUnpaidPayment.getId().value().toString(),
                        aUnpaidPayment.getVersion(),
                        aUnpaidPayment.getStatus().name(),
                        aUnpaidPayment.getTransactionId()
                ));
                this.paymentRepository.save(aUnpaidPayment);
            }
            default -> throw DomainException.with("Unknown payment status: " + notification.currentStatus());
        }
    }
}
