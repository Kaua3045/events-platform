package com.kaua.events.platform;

import com.kaua.events.platform.infrastructure.jdbc.JdbcClientAdapter;
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

    @Autowired
    private JdbcClient jdbcClient;

    private UserJdbcRepository userJdbcRepository;

    @BeforeEach
    void setUp() {
        this.userJdbcRepository = new UserJdbcRepository(new JdbcClientAdapter(jdbcClient));
    }

    protected int countUsers() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE);
    }

    public UserJdbcRepository userRepository() {
        return userJdbcRepository;
    }
}
