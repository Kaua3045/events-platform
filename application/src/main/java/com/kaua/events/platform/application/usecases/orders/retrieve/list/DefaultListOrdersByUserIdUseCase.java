package com.kaua.events.platform.application.usecases.orders.retrieve.list;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.Objects;

public class DefaultListOrdersByUserIdUseCase extends ListOrdersByUserIdUseCase {

    private final OrderRepository orderRepository;

    public DefaultListOrdersByUserIdUseCase(final OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    @Override
    public Pagination<ListOrdersByUserIdOutput> execute(final SearchQuery input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(ListOrdersByUserIdUseCase.class);

        return this.orderRepository.listAll(input)
                .map(ListOrdersByUserIdOutput::from);
    }
}
