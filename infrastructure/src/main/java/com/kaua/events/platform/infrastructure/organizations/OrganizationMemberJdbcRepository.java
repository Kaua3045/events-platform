package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;

@Component
public class OrganizationMemberJdbcRepository implements OrganizationMemberRepository {

    private static final Logger log = LoggerFactory.getLogger(OrganizationMemberJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OrganizationMemberJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrganizationMember save(final OrganizationMember organizationMember) {
        if (organizationMember.getVersion() == 0) {
            log.debug("Creating new organization member: {}", organizationMember);
            create(organizationMember);
            log.info("Created new organization member: {}", organizationMember);
        }

        organizationMember.incrementVersion();
        return organizationMember;
    }

    private void create(final OrganizationMember aOrganizationMember) {
        final var aSql = """
                INSERT INTO organization_members (id, version, organization_id, user_id, member_role, created_at, updated_at)
                VALUES (:id, (:version + 1), :organizationId, :userId, :memberRole, :createdAt, :updatedAt)
                """;

        executeUpdate(aSql, aOrganizationMember);
    }

    private int executeUpdate(final String aSql, final OrganizationMember aOrganizationMember) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aOrganizationMember.getId().value().toString());
        aParams.put("version", aOrganizationMember.getVersion());
        aParams.put("organizationId", aOrganizationMember.getOrganizationId().value().toString());
        aParams.put("userId", aOrganizationMember.getUserId().value().toString());
        aParams.put("memberRole", aOrganizationMember.getMemberRole().name());
        aParams.put("createdAt", aOrganizationMember.getCreatedAt());
        aParams.put("updatedAt", aOrganizationMember.getUpdatedAt());

        return databaseClient.update(aSql, aParams);
    }
}
