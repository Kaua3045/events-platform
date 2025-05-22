package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import com.kaua.events.platform.infrastructure.jdbc.JdbcUtils;
import com.kaua.events.platform.infrastructure.jdbc.RowMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class OrganizationMemberJdbcRepository implements OrganizationMemberRepository {

    private static final Logger log = LoggerFactory.getLogger(OrganizationMemberJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OrganizationMemberJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public Optional<OrganizationMember> memberOfUserId(final String userId) {
        final var aSql = "SELECT * FROM organization_members WHERE user_id = :userId";
        return this.databaseClient.queryOne(aSql, Map.of("userId", userId), memberMapper());
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

    private RowMap<OrganizationMember> memberMapper() {
        return rs ->
                OrganizationMember.with(
                        new OrganizationMemberID(ULID.fromString(rs.getString("id"))),
                        rs.getLong("version"),
                        new OrganizationID(ULID.fromString(rs.getString("organization_id"))),
                        new UserID(ULID.fromString(rs.getString("user_id"))),
                        OrganizationMemberRole.from(rs.getString("member_role")).orElse(null),
                        JdbcUtils.getInstant(rs, "created_at"),
                        JdbcUtils.getInstant(rs, "updated_at")
                );
    }
}
