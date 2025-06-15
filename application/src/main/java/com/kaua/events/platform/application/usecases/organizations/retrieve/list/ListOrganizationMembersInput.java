package com.kaua.events.platform.application.usecases.organizations.retrieve.list;

import com.kaua.events.platform.domain.pagination.SearchQuery;

public record ListOrganizationMembersInput(
        String organizationId,
        SearchQuery query
) {

    public static ListOrganizationMembersInput with(final String aOrganizationId, final SearchQuery aQuery) {
        return new ListOrganizationMembersInput(aOrganizationId, aQuery);
    }
}
