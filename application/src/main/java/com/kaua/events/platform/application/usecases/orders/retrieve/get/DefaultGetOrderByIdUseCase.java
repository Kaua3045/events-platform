package com.kaua.events.platform.application.usecases.orders.retrieve.get;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.Order;

import java.util.Objects;

public class DefaultGetOrderByIdUseCase extends GetOrderByIdUseCase {

    private final OrderRepository orderRepository;

    public DefaultGetOrderByIdUseCase(final OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    @Override
    public GetOrderByIdOutput execute(final GetOrderByIdInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(GetOrderByIdUseCase.class);

        return this.orderRepository.orderOfId(input.orderId())
                .map(GetOrderByIdOutput::from)
                .orElseThrow(NotFoundException.with(Order.class, input.orderId()));
    }
}
