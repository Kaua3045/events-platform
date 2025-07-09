package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

public interface EventRepository {

    boolean existsByTitleAndOrganizationId(String title, String id);

    Pagination<Event> listAll(SearchQuery query);

    Event save(Event event);
}
