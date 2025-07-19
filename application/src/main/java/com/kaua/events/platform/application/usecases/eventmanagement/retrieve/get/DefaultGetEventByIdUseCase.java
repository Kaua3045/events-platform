package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.exceptions.NotFoundException;

import java.util.Objects;

public class DefaultGetEventByIdUseCase extends GetEventByIdUseCase {

    private final EventRepository eventRepository;

    public DefaultGetEventByIdUseCase(final EventRepository eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
    }

    @Override
    public GetEventByIdOutput execute(final GetEventByIdInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(GetEventByIdUseCase.class);

        // TODO no futuro buscar o membro (userId) autenticado e validar se é mesmo da organizacao do evento
        final var aEvent = this.eventRepository.eventOfId(input.eventId())
                .orElseThrow(NotFoundException.with(Event.class, input.eventId()));

        return GetEventByIdOutput.from(aEvent);
    }
}
