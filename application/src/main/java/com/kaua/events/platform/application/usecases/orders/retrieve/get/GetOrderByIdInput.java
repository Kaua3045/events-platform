package com.kaua.events.platform.application.usecases.orders.retrieve.get;

public record GetOrderByIdInput(
        String orderId
) {

    public static GetOrderByIdInput with(final String orderId) {
        return new GetOrderByIdInput(orderId);
    }
}
