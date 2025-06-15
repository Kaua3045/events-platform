package com.kaua.events.platform.application.usecases.organizations.retrieve.list;

import com.kaua.events.platform.application.UseCase;
import com.kaua.events.platform.domain.pagination.Pagination;

public abstract class ListOrganizationMembersUseCase extends
        UseCase<ListOrganizationMembersInput, Pagination<ListOrganizationMembersOutput>> {
}
