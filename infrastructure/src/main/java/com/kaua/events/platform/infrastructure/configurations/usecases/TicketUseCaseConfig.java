package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.create.DefaultCreateTicketUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TicketUseCaseConfig {

    @Bean
    public CreateTicketUseCase createTicketUseCase(
            final TicketRepository ticketRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final EventRepository eventRepository
    ) {
        return new DefaultCreateTicketUseCase(
                ticketRepository,
                organizationMemberRepository,
                eventRepository
        );
    }
}
