package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutUseCase;
import com.kaua.events.platform.application.usecases.orders.create.DefaultCreateCheckoutUseCase;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.DefaultListOrdersByUserIdUseCase;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersByUserIdUseCase;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.application.wrapper.TransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OrderUseCaseConfig {

    @Bean
    public CreateCheckoutUseCase createCheckoutUseCase(
            final OrderRepository orderRepository,
            final TicketRepository ticketRepository,
            final TracerWrapper tracerWrapper,
            final TransactionManager transactionManager
    ) {
        return new DefaultCreateCheckoutUseCase(
                orderRepository,
                ticketRepository,
                tracerWrapper,
                transactionManager
        );
    }

    @Bean
    public ListOrdersByUserIdUseCase listOrdersByUserIdUseCase(
            final OrderRepository orderRepository
    ) {
        return new DefaultListOrdersByUserIdUseCase(
                orderRepository
        );
    }
}
