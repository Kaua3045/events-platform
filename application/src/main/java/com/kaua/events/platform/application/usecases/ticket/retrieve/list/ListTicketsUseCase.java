package com.kaua.events.platform.application.usecases.ticket.retrieve.list;

import com.kaua.events.platform.application.UseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

public abstract class ListTicketsUseCase extends UseCase<SearchQuery, Pagination<ListTicketsOutput>> {
}
