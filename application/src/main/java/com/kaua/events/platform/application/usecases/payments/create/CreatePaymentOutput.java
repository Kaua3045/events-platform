package com.kaua.events.platform.application.usecases.payments.create;

import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.domain.payments.PaymentDetails;

public record CreatePaymentOutput(
        String paymentId,
        PaymentDetails paymentDetails
) {

    public static CreatePaymentOutput from(final Payment aPayment) {
        return new CreatePaymentOutput(
                aPayment.getId().value().toString(),
                aPayment.getPaymentDetails()
        );
    }
}
