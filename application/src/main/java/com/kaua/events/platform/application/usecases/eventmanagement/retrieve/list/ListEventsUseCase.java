package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list;

import com.kaua.events.platform.application.UseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

public abstract class ListEventsUseCase extends UseCase<SearchQuery, Pagination<ListEventsOutput>> {
}
