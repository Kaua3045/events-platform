package com.kaua.events.platform.infrastructure.oauth.code;

import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.domain.auth.code.AuthorizationCode;
import com.kaua.events.platform.domain.auth.code.AuthorizationCodeID;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
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
public class AuthorizationCodeJdbcRepository implements AuthorizationCodeRepository {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationCodeJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public AuthorizationCodeJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public Optional<AuthorizationCode> authorizationCodeOfCode(final String code) {
        final var aSql = "SELECT * FROM authorization_codes WHERE code = :code";
        return this.databaseClient.queryOne(aSql, Map.of("code", code), authCodeMapper());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public AuthorizationCode save(final AuthorizationCode authorizationCode) {
        if (authorizationCode.getVersion() == 0) {
            log.debug("Creating new authorization code: {}", authorizationCode);
            create(authorizationCode);
            log.info("Created authorization code {}", authorizationCode);
        } else {
            log.debug("Updating authorization code: {}", authorizationCode);
            update(authorizationCode);
            log.info("Updated authorization code {}", authorizationCode);
        }

        authorizationCode.incrementVersion();
        return authorizationCode;
    }

    private void create(final AuthorizationCode authorizationCode) {
        final var aSql = """
                INSERT INTO authorization_codes (id, version, code, client_id, user_id, redirect_uri, code_challenge, code_challenge_method, is_used, expiration_date, created_at, updated_at)
                VALUES (:id, (:version + 1), :code, :client_id, :user_id, :redirect_uri, :code_challenge, :code_challenge_method, :is_used, :expiration_date, :created_at, :updated_at)
                """;
        executeUpdate(aSql, authorizationCode);
    }

    private void update(final AuthorizationCode authorizationCode) {
        final var aSql = """
                UPDATE authorization_codes
                SET
                    version = :version + 1,
                    code = :code,
                    client_id = :client_id,
                    user_id = :user_id,
                    redirect_uri = :redirect_uri,
                    code_challenge = :code_challenge,
                    code_challenge_method = :code_challenge_method,
                    is_used = :is_used,
                    expiration_date = :expiration_date,
                    created_at = :created_at,
                    updated_at = :updated_at
                WHERE id = :id AND version = :version
                """;

        if (executeUpdate(aSql, authorizationCode) == 0) {
            throw InternalErrorException.with("Conflict on update of authorization code");
        }
    }

    private int executeUpdate(final String aSql, final AuthorizationCode authorizationCode) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", authorizationCode.getId().value().toString()); // TODO verify this
        aParams.put("version", authorizationCode.getVersion());
        aParams.put("code", authorizationCode.getCode());
        aParams.put("client_id", authorizationCode.getClientId());
        aParams.put("user_id", authorizationCode.getUserId().value().toString());
        aParams.put("redirect_uri", authorizationCode.getRedirectUri());
        aParams.put("code_challenge", authorizationCode.getCodeChallenge());
        aParams.put("code_challenge_method", authorizationCode.getCodeChallengeMethod());
        aParams.put("is_used", authorizationCode.isUsed());
        aParams.put("expiration_date", authorizationCode.getExpirationDate());
        aParams.put("created_at", authorizationCode.getCreatedAt());
        aParams.put("updated_at", authorizationCode.getUpdatedAt());
        return this.databaseClient.update(aSql, aParams);
    }

    private RowMap<AuthorizationCode> authCodeMapper() {
        return rs ->
                AuthorizationCode.with(
                        new AuthorizationCodeID(ULID.fromString(rs.getString("id"))),
                        rs.getLong("version"),
                        rs.getString("code"),
                        rs.getString("client_id"),
                        new UserID(ULID.fromString(rs.getString("user_id"))),
                        rs.getString("redirect_uri"),
                        rs.getString("code_challenge"),
                        rs.getString("code_challenge_method"),
                        rs.getBoolean("is_used"),
                        JdbcUtils.getInstant(rs, "expiration_date"),
                        JdbcUtils.getInstant(rs, "created_at"),
                        JdbcUtils.getInstant(rs, "updated_at")
                );
    }
}
