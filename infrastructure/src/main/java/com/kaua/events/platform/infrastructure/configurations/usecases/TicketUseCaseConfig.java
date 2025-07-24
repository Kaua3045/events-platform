package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.create.DefaultCreateTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.delete.soft.DefaultSoftDeleteTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.delete.soft.SoftDeleteTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.get.DefaultGetTicketByIdUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.get.GetTicketByIdUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.list.DefaultListTicketsUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.list.ListTicketsUseCase;
import com.kaua.events.platform.application.usecases.ticket.update.DefaultUpdateTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.update.UpdateTicketUseCase;
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

    @Bean
    public ListTicketsUseCase listTicketsUseCase(
            final TicketRepository ticketRepository
    ) {
        return new DefaultListTicketsUseCase(ticketRepository);
    }

    @Bean
    public UpdateTicketUseCase updateTicketUseCase(
            final TicketRepository ticketRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final EventRepository eventRepository
    ) {
        return new DefaultUpdateTicketUseCase(
                ticketRepository,
                eventRepository,
                organizationMemberRepository
        );
    }

    @Bean
    public GetTicketByIdUseCase getTicketByIdUseCase(
            final TicketRepository ticketRepository
    ) {
        return new DefaultGetTicketByIdUseCase(ticketRepository);
    }

    @Bean
    public SoftDeleteTicketUseCase softDeleteTicketUseCase(
            final TicketRepository ticketRepository,
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository
    ) {
        return new DefaultSoftDeleteTicketUseCase(
                ticketRepository,
                eventRepository,
                organizationMemberRepository
        );
    }
}
