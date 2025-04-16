package com.kaua.events.platform;

import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.AuthorizationTokenRepository;
import com.kaua.events.platform.infrastructure.jdbc.JdbcClientAdapter;
import com.kaua.events.platform.infrastructure.oauth.code.AuthorizationCodeJdbcRepository;
import com.kaua.events.platform.infrastructure.oauth.token.AuthorizationTokenJdbcRepository;
import com.kaua.events.platform.infrastructure.users.UserJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

@DataJdbcTest
@Tag("integrationTest")
@ActiveProfiles("test-integration")
public abstract class AbstractRepositoryTest {

    private static final String USERS_TABLE = "users";
    private static final String AUTHORIZATION_CODE_TABLE = "authorization_codes";
    private static final String AUTHORIZATION_TOKEN_TABLE = "authorization_tokens";

    @Autowired
    private JdbcClient jdbcClient;

    private UserJdbcRepository userJdbcRepository;
    private AuthorizationCodeRepository authorizationCodeRepository;
    private AuthorizationTokenRepository authorizationTokenRepository;

    @BeforeEach
    void setUp() {
        this.userJdbcRepository = new UserJdbcRepository(new JdbcClientAdapter(jdbcClient));
        this.authorizationCodeRepository = new AuthorizationCodeJdbcRepository(new JdbcClientAdapter(jdbcClient));
        this.authorizationTokenRepository = new AuthorizationTokenJdbcRepository(new JdbcClientAdapter(jdbcClient));
    }

    protected int countUsers() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE);
    }

    protected int countAuthorizationCodes() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORIZATION_CODE_TABLE);
    }

    protected int countAuthorizationTokens() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORIZATION_TOKEN_TABLE);
    }

    public UserJdbcRepository userRepository() {
        return userJdbcRepository;
    }

    public AuthorizationCodeRepository authorizationCodeRepository() {
        return authorizationCodeRepository;
    }

    public AuthorizationTokenRepository authorizationTokenRepository() {
        return authorizationTokenRepository;
    }
}
