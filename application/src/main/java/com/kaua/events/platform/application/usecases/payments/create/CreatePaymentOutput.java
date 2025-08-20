package com.kaua.events.platform.application.usecases.payments.create;

import com.kaua.events.platform.domain.payments.Payment;

public record CreatePaymentOutput(
        String paymentId,
        String qrCode,
        String qrCodeImageUrl
) {

    public static CreatePaymentOutput from(final Payment aPayment) {
        return new CreatePaymentOutput(
                aPayment.getId().value().toString(),
                aPayment.getQrCode().orElse(null),
                aPayment.getQrCodeImageUrl().orElse(null)
        );
    }
}
