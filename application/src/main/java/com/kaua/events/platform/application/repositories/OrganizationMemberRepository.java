package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.organizations.OrganizationMember;

import java.util.Optional;

public interface OrganizationMemberRepository {

    Optional<OrganizationMember> memberOfUserId(String userId);

    OrganizationMember save(OrganizationMember organizationMember);
}
