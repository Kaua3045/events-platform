package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.organizations.Organization;

public interface OrganizationRepository {

    boolean existsByName(String name);

    boolean existsById(String id);

    Organization save(Organization organization);
}
