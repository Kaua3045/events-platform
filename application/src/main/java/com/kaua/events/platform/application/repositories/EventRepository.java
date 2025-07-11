package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.Optional;

public interface EventRepository {

    boolean existsByTitleAndOrganizationId(String title, String id);

    Pagination<Event> listAll(SearchQuery query);

    Optional<Event> eventOfId(String id);

    Event save(Event event);
}
