package com.kaua.events.platform.application.usecases.orders.update.status;

public record UpdateOrderStatusInput(
        String orderId,
        String status,
        String paymentId
) {

    public static UpdateOrderStatusInput with(
            final String aOrderId,
            final String aStatus,
            final String aPaymentId
    ) {
        return new UpdateOrderStatusInput(aOrderId, aStatus, aPaymentId);
    }
}
