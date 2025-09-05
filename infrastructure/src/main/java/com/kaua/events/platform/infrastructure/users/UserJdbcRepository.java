package com.kaua.events.platform.infrastructure.users;

import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.person.Document;
import com.kaua.events.platform.domain.person.DocumentFactory;
import com.kaua.events.platform.domain.users.*;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
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
public class UserJdbcRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public UserJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public Optional<User> userOfEmail(final String email) {
        final var aSql = "SELECT * FROM users where email = :email";
        return this.databaseClient.queryOne(aSql, Map.of("email", email), userMapper());
    }

    @Override
    public Optional<User> userOfId(final String id) {
        final var aSql = "SELECT * FROM users where id = :id";
        return this.databaseClient.queryOne(aSql, Map.of("id", id), userMapper());
    }

    @Override
    public boolean existsByEmail(final String aEmail) {
        final var aSql = "SELECT COUNT(*) FROM users WHERE email = :email";
        return this.databaseClient.count(aSql, Map.of("email", aEmail)) > 0;
    }

    @Override
    public boolean existsById(final String id) {
        final var aSql = "SELECT COUNT(*) FROM users WHERE id = :id";
        return this.databaseClient.count(aSql, Map.of("id", id)) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public User save(final User aUser) {
        if (aUser.getVersion() == 0) {
            log.debug("Creating a new user: {}", aUser);
            create(aUser);
            log.info("Created user {}", aUser);
        } else {
            log.debug("Updating user: {}", aUser);
            update(aUser);
            log.info("Updated user: {}", aUser);
        }

        aUser.incrementVersion();
        return aUser;
    }

    private void create(final User aUser) {
        final var aSql = """
                INSERT INTO users (id, version, first_name, last_name, email, password, role, document_number, document_type, phone_e164, created_at, updated_at)
                VALUES (:id, (:version + 1), :first_name, :last_name, :email, :password, :role, :document_number, :document_type, :phone_e164, :created_at, :updated_at)
                """;
        executeUpdate(aSql, aUser);
    }

    private void update(final User aUser) {
        final var aSql = """
                UPDATE users
                SET
                version = (:version + 1),
                first_name = :first_name,
                last_name = :last_name,
                email = :email,
                password = :password,
                role = :role,
                document_number = :document_number,
                document_type = :document_type,
                phone_e164 = :phone_e164,
                updated_at = :updated_at
                WHERE id = :id AND version = :version
                """;

        if (executeUpdate(aSql, aUser) == 0) {
            throw ConflictException.with("User with identifier %s and version %d does not match, user was updated by another transaction"
                    .formatted(aUser.getId().value().toString(), aUser.getVersion()));
        }
    }

    private int executeUpdate(final String aSql, final User aUser) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aUser.getId().value().toString()); // TODO verify this
        aParams.put("version", aUser.getVersion());
        aParams.put("first_name", aUser.getName().firstName());
        aParams.put("last_name", aUser.getName().lastName());
        aParams.put("email", aUser.getEmail().value());
        aParams.put("password", aUser.getPassword().value());
        aParams.put("role", aUser.getRole().name());
        aParams.put("document_number", aUser.getDocument().map(Document::value).orElse(null));
        aParams.put("document_type", aUser.getDocument().map(Document::type).orElse(null));
        aParams.put("phone_e164", aUser.getPhoneNumber().orElse(null));
        aParams.put("created_at", aUser.getCreatedAt());
        aParams.put("updated_at", aUser.getUpdatedAt());
        return this.databaseClient.update(aSql, aParams);
    }

    private RowMap<User> userMapper() {
        return rs ->
                User.with(
                        new UserID(ULID.fromString(rs.getString("id"))),
                        rs.getLong("version"),
                        new Name(rs.getString("first_name"), rs.getString("last_name")),
                        new Email(rs.getString("email")),
                        Password.of(rs.getString("password")),
                        UserRole.from(rs.getString("role")).orElseThrow(() -> NotFoundException.with("UserRole not found")),
                        rs.getString("document_type") != null ?
                                DocumentFactory.create(rs.getString("document_number"), rs.getString("document_type"))
                                : null,
                        rs.getString("phone_e164"),
                        JdbcUtils.getInstant(rs, "created_at"),
                        JdbcUtils.getInstant(rs, "updated_at")
                );
    }
}
