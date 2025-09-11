package com.kaua.events.platform.application.usecases.orders.update.status;

import com.kaua.events.platform.domain.orders.Order;

public record UpdateOrderStatusOutput(
        String orderId,
        String status
) {

    public static UpdateOrderStatusOutput from(final Order aOrder) {
        return new UpdateOrderStatusOutput(
                aOrder.getId().value().toString(),
                aOrder.getStatus().name()
        );
    }
}
