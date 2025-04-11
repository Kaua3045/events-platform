package com.kaua.events.platform.infrastructure.users;

import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.users.User;
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
public class UserJdbcRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public UserJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public boolean existsByEmail(final String aEmail) {
        final var aSql = "SELECT COUNT(*) FROM users WHERE email = :email";
        return this.databaseClient.count(aSql, Map.of("email", aEmail)) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public User save(final User aUser) {
        if (aUser.getVersion() == 0) {
            log.debug("Creating a new user: {}", aUser);
            create(aUser);
            log.info("Created user {}", aUser);
        }

        aUser.incrementVersion();
        return aUser;
    }

    private void create(final User aUser) {
        final var aSql = """
                INSERT INTO users (id, version, first_name, last_name, email, password, role, created_at, updated_at)
                VALUES (:id, (:version + 1), :first_name, :last_name, :email, :password, :role, :created_at, :updated_at)
                """;
        executeUpdate(aSql, aUser);
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
        aParams.put("created_at", aUser.getCreatedAt());
        aParams.put("updated_at", aUser.getUpdatedAt());
        return this.databaseClient.update(aSql, aParams);
    }
}
