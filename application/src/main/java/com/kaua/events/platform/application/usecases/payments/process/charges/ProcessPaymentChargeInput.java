package com.kaua.events.platform.application.usecases.payments.process.charges;

public record ProcessPaymentChargeInput(
        String notificationId
) {

    public static ProcessPaymentChargeInput with(final String notificationId) {
        return new ProcessPaymentChargeInput(notificationId);
    }
}
