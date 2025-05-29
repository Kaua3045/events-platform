package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.organizations.Organization;

import java.util.Optional;

public interface OrganizationRepository {

    boolean existsByName(String name);

    boolean existsById(String id);

    Optional<Organization> organizationOfId(String id);

    Organization save(Organization organization);
}
