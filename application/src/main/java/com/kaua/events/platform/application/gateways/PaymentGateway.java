package com.kaua.events.platform.application.gateways;

import com.kaua.events.platform.domain.payments.PaymentDetails;

public interface PaymentGateway {

    PaymentProcessResponse process(PaymentProcessRequest request);

    record PaymentProcessRequest(
            String transactionId,
            String orderId,
            PaymentDetails paymentDetails
    ) {
    }

    record PaymentProcessResponse(
            String qrCode,
            String qrCodeImageUrl,
            int expiresIn,
            PaymentProcessStatus status
    ) {
    }

    enum PaymentProcessStatus {
        ACTIVE,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}
