package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.organizations.Organization;

public interface OrganizationRepository {

    boolean existsByName(String name);

    Organization save(Organization organization);
}
