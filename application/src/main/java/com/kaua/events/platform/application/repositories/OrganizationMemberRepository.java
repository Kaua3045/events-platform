package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.organizations.OrganizationMember;

public interface OrganizationMemberRepository {

    OrganizationMember save(OrganizationMember organizationMember);
}
