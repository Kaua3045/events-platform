package com.kaua.events.platform.application.usecases.orders.retrieve.list;

import com.kaua.events.platform.application.UseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

public abstract class ListOrdersByUserIdUseCase extends UseCase<SearchQuery, Pagination<ListOrdersByUserIdOutput>> {
}
