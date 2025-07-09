package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.create.DefaultCreateEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.DefaultListEventsUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.ListEventsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EventUseCaseConfig {

    @Bean
    public CreateEventUseCase createEventUseCase(
            final EventRepository eventRepository
    ) {
        return new DefaultCreateEventUseCase(eventRepository);
    }

    @Bean
    public ListEventsUseCase listEventsUseCase(
            final EventRepository eventRepository
    ) {
        return new DefaultListEventsUseCase(eventRepository);
    }
}
