package com.kaua.events.platform.application.usecases.payments.create;

import com.kaua.events.platform.domain.payments.PaymentDetails;

public record CreatePaymentInput(
        PaymentDetails paymentDetails,
        String orderId,
        String traceId
) {

    public static CreatePaymentInput with(
            final PaymentDetails aPaymentDetails,
            final String aOrderId,
            final String aTraceId
    ) {
        return new CreatePaymentInput(aPaymentDetails, aOrderId, aTraceId);
    }
}
