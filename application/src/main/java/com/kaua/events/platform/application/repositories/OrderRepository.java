package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.Optional;

public interface OrderRepository {

    Optional<Order> orderOfId(String id);

    Pagination<Order> listAll(SearchQuery query);

    Order save(Order order);
}
