package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository {

    Optional<OrganizationMember> memberOfUserId(String userId);

    Pagination<OrganizationMember> membersOfOrganizationId(String organizationId, SearchQuery query);

    OrganizationMember save(OrganizationMember organizationMember);
}
