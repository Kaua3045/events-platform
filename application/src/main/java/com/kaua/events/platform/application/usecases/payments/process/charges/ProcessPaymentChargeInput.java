package com.kaua.events.platform.application.usecases.payments.process.charges;

public record ProcessPaymentChargeInput(
        String notificationId,
        String method
) {

    public static ProcessPaymentChargeInput with(final String notificationId, final String method) {
        return new ProcessPaymentChargeInput(notificationId, method);
    }
}
