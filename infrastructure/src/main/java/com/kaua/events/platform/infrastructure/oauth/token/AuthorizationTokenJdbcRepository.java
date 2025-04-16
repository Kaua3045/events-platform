package com.kaua.events.platform.infrastructure.oauth.token;

import com.kaua.events.platform.application.repositories.AuthorizationTokenRepository;
import com.kaua.events.platform.domain.auth.token.AuthorizationToken;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenID;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import com.kaua.events.platform.infrastructure.jdbc.JdbcUtils;
import com.kaua.events.platform.infrastructure.jdbc.RowMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class AuthorizationTokenJdbcRepository implements AuthorizationTokenRepository {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationTokenJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public AuthorizationTokenJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public List<AuthorizationToken> tokensOfSub(final String sub) {
        final var aSql = "SELECT * FROM authorization_tokens WHERE user_id = :user_id AND revoked = false AND expires_in > now()";
        return this.databaseClient.query(aSql, Map.of("user_id", sub), tokenMapper());
    }

    @Override
    public Optional<AuthorizationToken> tokenOfJti(final String jti) {
        final var aSql = "SELECT * FROM authorization_tokens WHERE jti = :jti";
        return this.databaseClient.queryOne(aSql, Map.of("jti", jti), tokenMapper());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public AuthorizationToken save(final AuthorizationToken authorizationToken) {
        if (authorizationToken.getVersion() == 0) {
            log.debug("Creating new authorization token: {}", authorizationToken);
            create(authorizationToken);
            log.info("Created authorization token {}", authorizationToken);
        } else {
            log.debug("Updating authorization token: {}", authorizationToken);
            update(authorizationToken);
            log.info("Updated authorization token {}", authorizationToken);
        }

        authorizationToken.incrementVersion();
        return authorizationToken;
    }

    private void create(AuthorizationToken authorizationToken) {
        final var aSql = """
                INSERT INTO authorization_tokens (id, version, jti, client_id, type, expires_in, issued_at, revoked, user_id)
                VALUES (:id, (:version + 1), :jti, :client_id, :type, :expires_in, :issued_at, :revoked, :user_id)
                """;
        executeUpdate(aSql, authorizationToken);
    }

    private void update(AuthorizationToken authorizationToken) {
        final var aSql = """
                UPDATE authorization_tokens
                SET
                    version = :version + 1,
                    jti = :jti,
                    client_id = :client_id,
                    type = :type,
                    expires_in = :expires_in,
                    issued_at = :issued_at,
                    revoked = :revoked
                WHERE id = :id AND version = :version
                """;

        if (executeUpdate(aSql, authorizationToken) == 0) {
            throw InternalErrorException.with("Conflict on update of authorization token");
        }
    }

    private int executeUpdate(final String aSql, final AuthorizationToken authorizationToken) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", authorizationToken.getId().value().toString());
        aParams.put("version", authorizationToken.getVersion());
        aParams.put("jti", authorizationToken.getTokenJTI());
        aParams.put("client_id", authorizationToken.getClientId());
        aParams.put("type", authorizationToken.getType().name());
        aParams.put("expires_in", authorizationToken.getExpiresIn());
        aParams.put("issued_at", authorizationToken.getIssuedAt());
        aParams.put("revoked", authorizationToken.isRevoked());
        aParams.put("user_id", authorizationToken.getUserId().orElse(null));
        return this.databaseClient.update(aSql, aParams);
    }

    private RowMap<AuthorizationToken> tokenMapper() {
        return rs ->
                AuthorizationToken.with(
                        new AuthorizationTokenID(ULID.fromString(rs.getString("id"))),
                        rs.getLong("version"),
                        rs.getString("jti"),
                        AuthorizationTokenType.from(rs.getString("type")).orElseThrow(() -> NotFoundException.with("Token type was not found")),
                        JdbcUtils.getInstant(rs, "expires_in"),
                        JdbcUtils.getInstant(rs, "issued_at"),
                        rs.getBoolean("revoked"),
                        rs.getString("client_id"),
                        rs.getString("user_id")
                );
    }
}
