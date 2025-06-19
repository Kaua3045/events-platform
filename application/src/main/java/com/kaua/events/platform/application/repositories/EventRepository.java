package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.eventmanagement.Event;

public interface EventRepository {

    boolean existsByTitleAndOrganizationId(String title, String id);

    Event save(Event event);
}
