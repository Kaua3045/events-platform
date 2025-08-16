package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.orders.Order;

public interface OrderRepository {

    Order save(Order order);
}
