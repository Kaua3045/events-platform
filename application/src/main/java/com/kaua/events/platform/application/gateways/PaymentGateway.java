package com.kaua.events.platform.application.gateways;

import com.kaua.events.platform.domain.payments.PaymentDetails;

import java.util.List;

public interface PaymentGateway {

    PaymentProcessResponse process(PaymentProcessRequest request);

    PaymentNotification getNotifications(String notificationId);

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
        WAITING,
        COMPLETED,
        FAILED,
        EXPIRED
    }

    record PaymentNotification(
            int code,
            List<PaymentNotificationData> data
    ) {
    }

    record PaymentNotificationData(
            long id,
            String type,
            String customId,
            String currentStatus,
            long chargeId,
            String createdAt
    ) {
    }
}
