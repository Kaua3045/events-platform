package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.Objects;

public class DefaultListEventsUseCase extends ListEventsUseCase {

    private final EventRepository eventRepository;

    public DefaultListEventsUseCase(final EventRepository eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
    }

    @Override
    public Pagination<ListEventsOutput> execute(final SearchQuery input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(ListEventsUseCase.class);

        return this.eventRepository.listAll(input)
                .map(ListEventsOutput::from);
    }
}
