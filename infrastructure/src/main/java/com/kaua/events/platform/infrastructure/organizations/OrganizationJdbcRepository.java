package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class OrganizationJdbcRepository implements OrganizationRepository {

    private static final Logger log = LoggerFactory.getLogger(OrganizationJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OrganizationJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public boolean existsByName(final String name) {
        final var aSql = "SELECT COUNT(*) FROM organizations WHERE name = :name";
        return this.databaseClient.count(aSql, Map.of("name", name)) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Organization save(final Organization organization) {
        if (organization.getVersion() == 0) {
            log.debug("Creating new organization: {}", organization);
            create(organization);
            log.info("Created new organization: {}", organization);
        }

        organization.incrementVersion();
        return organization;
    }

    private void create(final Organization aOrganization) {
        final var aSql = """
                INSERT INTO organizations (id, version, name, description, is_deleted, created_at, updated_at, deleted_at)
                VALUES (:id, (:version + 1), :name, :description, :is_deleted, :created_at, :updated_at, :deleted_at)
                """;

        executeUpdate(aSql, aOrganization);
    }

    private int executeUpdate(final String aSql, final Organization aOrganization) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aOrganization.getId().value().toString());
        aParams.put("version", aOrganization.getVersion());
        aParams.put("name", aOrganization.getName());
        aParams.put("description", aOrganization.getDescription().orElse(null));
        aParams.put("is_deleted", aOrganization.isDeleted());
        aParams.put("created_at", aOrganization.getCreatedAt());
        aParams.put("updated_at", aOrganization.getUpdatedAt());
        aParams.put("deleted_at", aOrganization.getDeletedAt().orElse(null));

        return this.databaseClient.update(aSql, aParams);
    }
}
